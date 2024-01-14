package bms.usagebilling.service.resources

import bms.usagebilling.db.ClickhouseOptions.asyncInsertNoWaitOptions
import bms.usagebilling.db.ClickhouseOptions.asyncInsertWaitOptions
import bms.usagebilling.db.DatabaseManager
import bms.usagebilling.db.DatabaseManager.customOptionsArray
import com.clickhouse.client.ClickHouseClient
import com.clickhouse.client.ClickHouseProtocol
import com.clickhouse.client.ClickHouseResponse
import com.clickhouse.client.ClickHouseResponseSummary
import com.clickhouse.data.ClickHouseDataStreamFactory
import com.clickhouse.data.ClickHouseFormat
import com.clickhouse.data.ClickHouseValue
import com.clickhouse.data.value.ClickHouseArrayValue
import com.clickhouse.data.value.ClickHouseUuidValue
import kotlinx.uuid.UUID
import kotlinx.uuid.toJavaUUID
import java.util.concurrent.CompletableFuture

object ResourceService {


    fun insertUsages(
        resources: List<UsageResource>,
        wait: Boolean = false
    ): ClickHouseResponseSummary {
        ClickHouseClient.newInstance(DatabaseManager.server.protocol).use { client ->
            val request = client.write(DatabaseManager.server)
                .table(DatabaseManager.resourcesTable)
                .customOptionsArray(if (wait) asyncInsertWaitOptions else asyncInsertNoWaitOptions)
                .format(ClickHouseFormat.RowBinary)
            val config = request.config
            var future: CompletableFuture<ClickHouseResponse>
            ClickHouseDataStreamFactory.getInstance()
                .createPipedOutputStream(config, null as Runnable?).use { stream ->
                    // in async mode, which is default, execution happens in a worker thread
                    future = request.data(stream.inputStream).execute()
                    resources.forEach {
                        //println("Writing: ${it.id} -> ${it.end}")
                        it.writeToClickhouse(stream)
                    }
                    //println("Wrote all?")
                }

            future.get().use { response ->
                val summary = response.summary
                return summary
            }
        }
    }

    private fun doSelectUsageResources(query: String, params: Array<ClickHouseValue>): List<UsageResource> {
        return ClickHouseClient.newInstance(ClickHouseProtocol.HTTP).use { client ->
            client.read(DatabaseManager.server)
                .table(DatabaseManager.eventsTable)
                .format(ClickHouseFormat.RowBinaryWithNamesAndTypes)
                .query(query)
                .params(params)
                .executeAndWait().use { response ->
                    val resp = response.records().map { UsageResource.fromClickhouseRecord(it) }

                    val summary = response.summary

                    println("Read: ${summary.readRows}, read total: ${summary.totalRowsToRead}")

                    resp
                }
        }
    }

    fun selectResources(
        organization: UUID,
        group: UUID,
        ids: List<UUID>
    ): List<UsageResource> {
        val query = "select * from resources where organization = :organization and group = :group and id in :ids"
        val params: Array<ClickHouseValue> = listOf(
            ClickHouseUuidValue.of(organization.toJavaUUID()),
            ClickHouseUuidValue.of(group.toJavaUUID()),
            ClickHouseArrayValue.of(ids.map { it.toJavaUUID() }.toTypedArray())
        ).toTypedArray()

        return doSelectUsageResources(query, params)
    }

    /**
     * WARNING: May select more elements, as the group and id is a logical or!
     */
    fun selectResources(
        organization: UUID,
        groups: List<UUID>,
        ids: List<UUID>
    ): List<UsageResource> {
        val query = "select * from resources where organization = :organization and group in :groups and id in :ids"
        val params: Array<ClickHouseValue> = listOf(
            ClickHouseUuidValue.of(organization.toJavaUUID()),
            ClickHouseArrayValue.of(groups.map { it.toJavaUUID() }.toTypedArray()),
            ClickHouseArrayValue.of(ids.map { it.toJavaUUID() }.toTypedArray())
        ).toTypedArray()

        return doSelectUsageResources(query, params)
    }

    fun endUseOfResources(
        organization: UUID,
        resources: List<UsageEndResource>,
        wait: Boolean = false
    ) {
        val groupedResources = resources.groupBy { it.group }

        val storedResources = selectResources(
            organization,
            groupedResources.keys.toList(),
            resources.map { it.id }).associateBy { Pair(it.group, it.id) }

        val newResources = resources.mapNotNull {
            storedResources[Pair(it.group, it.id)]?.copy(end = it.end).also { e ->
                if (e == null) {
                    println("NO SUCH resource: $it")
                }
            }
        }

        insertUsages(newResources, wait)
    }
}
