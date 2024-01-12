package bms.usagebilling.service.events

import bms.usagebilling.db.DatabaseManager
import bms.usagebilling.db.JAVA_UTC_TIMEZONE
import com.clickhouse.client.ClickHouseClient
import com.clickhouse.client.ClickHouseException
import com.clickhouse.client.ClickHouseProtocol
import com.clickhouse.client.ClickHouseResponse
import com.clickhouse.client.ClickHouseResponseSummary
import com.clickhouse.data.ClickHouseDataStreamFactory
import com.clickhouse.data.ClickHouseFormat
import com.clickhouse.data.ClickHouseValue
import com.clickhouse.data.value.ClickHouseArrayValue
import com.clickhouse.data.value.ClickHouseDateTimeValue
import com.clickhouse.data.value.ClickHouseUuidValue
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.uuid.UUID
import kotlinx.uuid.toJavaUUID
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

object EventService {

    /**
     * @param organizationId: required - organization to show
     * @param projectId: optional - project to show
     * @param filterEvents: optional - list of accepted event names
     * @param limit: optional - int to skip & int to limit
     * @param duringTime: optional - timestamp (in UTC!) to begin filter and timestamp (in UTC!) to end filter
     * @param orderDescending: default=false - order by timestamp descending instead of ascending
     */
    fun listEvents(
        organizationId: UUID,
        projectId: UUID? = null,
        filterEvents: List<String>? = null,
        limit: Pair<Int, Int>? = null,
        duringTime: Pair<LocalDateTime, LocalDateTime>? = null,
        orderDescending: Boolean = false,
    ) = doQuery(
        operation = Operation.LIST,
        organization = organizationId,
        group = projectId,
        filterEvents = filterEvents,
        limit = limit,
        duringTime = duringTime,
        orderDescending = orderDescending
    ) { response: ClickHouseResponse ->
        val resp = response.records().map { UsageEvent.fromClickhouseRecord(it) }

        val summary = response.summary
        val totalRows = summary.totalRowsToRead

        println("Read: $totalRows")

        resp
    }

    fun checkDatabaseStatus() = runCatching {
        ClickHouseClient.newInstance().use { client ->
            client.read(DatabaseManager.server)
                .table(DatabaseManager.eventsTable)
                .format(ClickHouseFormat.RowBinaryWithNamesAndTypes)
                .query("select 1")
                .executeAndWait().use {
                    it.firstRecord().getValue(0).asInteger() == 0
                }
        }
    }

    fun countEvents(
        organizationId: UUID,
        projectId: UUID? = null,
        filterEvents: List<String>? = null,
        limit: Pair<Int, Int>? = null,
        duringTime: Pair<LocalDateTime, LocalDateTime>? = null,
    ) = doQuery(
        operation = Operation.COUNT,
        organization = organizationId,
        group = projectId,
        filterEvents = filterEvents,
        limit = limit,
        duringTime = duringTime,
        orderDescending = false
    ) { response: ClickHouseResponse ->
        val count = response.records().first().getValue(0).asLong()

        val summary = response.summary
        val totalRows = summary.totalRowsToRead

        println("Read: $totalRows")

        count
    }

    private enum class Operation {
        LIST,
        COUNT
    }

    /**
     * @param organization: required - organization to show
     * @param group: optional - project to show
     * @param filterEvents: optional - list of accepted event names
     * @param limit: optional - int to skip & int to limit
     * @param duringTime: optional - timestamp (in UTC!) to begin filter and timestamp (in UTC!) to end filter
     * @param orderDescending: default=false - order by timestamp descending instead of ascending
     */
    private fun <T> doQuery(
        operation: Operation,
        organization: UUID,
        group: UUID? = null,
        filterEvents: List<String>? = null,
        limit: Pair<Int, Int>? = null,
        duringTime: Pair<LocalDateTime, LocalDateTime>? = null,
        orderDescending: Boolean = false,
        callback: (ClickHouseResponse) -> T,
    ): T =
        ClickHouseClient.newInstance(ClickHouseProtocol.HTTP).use { client ->
            val query = StringBuilder(
                when (operation) {
                    Operation.LIST -> "SELECT * FROM ${DatabaseManager.eventsTable}"
                    Operation.COUNT -> "SELECT count(*) FROM ${DatabaseManager.eventsTable}"
                }
            )

            query.append(" WHERE organization = :organization")

            val params = arrayListOf<ClickHouseValue>(ClickHouseUuidValue.of(organization.toJavaUUID()))

            if (group != null) {
                query.append(" AND grouping = :grouping")
                params.add(ClickHouseUuidValue.of(group.toJavaUUID()))
            }

            if (filterEvents != null) {
                query.append(" AND name in :name")
                params.add(ClickHouseArrayValue.of(filterEvents.toTypedArray()))
            }

            if (duringTime != null) {
                val t1 = duringTime.first.toJavaLocalDateTime()
                val t2 = duringTime.second.toJavaLocalDateTime()
                query.append(" AND timestamp >= :t1 AND timestamp < :t2")

                println("Sorting by > $t1 and < $t2")

                params.add(ClickHouseDateTimeValue.of(t1, 3, JAVA_UTC_TIMEZONE).also { println("T1: $it") })
                params.add(ClickHouseDateTimeValue.of(t2, 3, JAVA_UTC_TIMEZONE).also { println("T2: $it") })
            }

            query.append(" ORDER BY timestamp")

            if (orderDescending) {
                query.append(" DESC")
            }

            if (limit != null) {
                query.append(" LIMIT ${limit.first}, ${limit.second}")
            }

            println("QUERY: ${query.toString()}")
            println("PARAMS: ${params.toString()}")

            client.read(DatabaseManager.server)
                .table(DatabaseManager.eventsTable)
                .format(ClickHouseFormat.RowBinaryWithNamesAndTypes)
                .query(query.toString())
                .params(params.toTypedArray())
                .executeAndWait().use { response ->
                    callback.invoke(response)
                }
        }

    fun insertEvents(events: List<UsageEvent?>): ClickHouseResponseSummary? {
        try {
            ClickHouseClient.newInstance(DatabaseManager.server.protocol).use { client ->
                val request = client.read(DatabaseManager.server).write().table(DatabaseManager.eventsTable)
                    .options(DatabaseManager.insertProps)
                    .format(ClickHouseFormat.RowBinary)
                val config = request.config
                var future: CompletableFuture<ClickHouseResponse>
                ClickHouseDataStreamFactory.getInstance()
                    .createPipedOutputStream(config, null as Runnable?).use { stream ->
                        // in async mode, which is default, execution happens in a worker thread
                        future = request.data(stream.inputStream).execute()
                        events.forEach {
                            it?.writeToClickhouse(stream)
                        }
                    }

                future.get().use { response ->
                    val summary = response.summary
                    return summary
                }
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw ClickHouseException.forCancellation(e, DatabaseManager.server)
        } catch (e: ExecutionException) {
            throw ClickHouseException.of(e, DatabaseManager.server)
        } catch (e: IOException) {
            throw ClickHouseException.of(e, DatabaseManager.server)
        }
    }

}
