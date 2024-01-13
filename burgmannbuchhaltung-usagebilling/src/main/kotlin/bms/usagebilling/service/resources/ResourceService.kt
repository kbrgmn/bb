package bms.usagebilling.service.resources

import bms.usagebilling.db.DatabaseManager
import com.clickhouse.client.ClickHouseClient
import com.clickhouse.client.ClickHouseProtocol
import com.clickhouse.client.ClickHouseResponse
import com.clickhouse.client.ClickHouseResponseSummary
import com.clickhouse.data.ClickHouseDataStreamFactory
import com.clickhouse.data.ClickHouseFormat
import com.clickhouse.data.value.ClickHouseArrayValue
import com.clickhouse.data.value.ClickHouseUuidValue
import kotlinx.datetime.Clock
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import kotlinx.uuid.toJavaUUID
import java.util.concurrent.CompletableFuture
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

object ResourceService {

    fun insertUsages(
        resources: List<UsageResource>
    ): ClickHouseResponseSummary {
        ClickHouseClient.newInstance(DatabaseManager.server.protocol).use { client ->
            val request = client.read(DatabaseManager.server)
                .write()
                .table(DatabaseManager.resourcesTable)
                .options(DatabaseManager.insertProps)
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

    fun selectResources(
        organization: UUID,
        group: UUID,
        ids: List<UUID>
    ): List<UsageResource> {
        val query = "select * from resources where organization = :organization and group = :group and id in :ids"
        val params = listOf(
            ClickHouseUuidValue.of(organization.toJavaUUID()),
            ClickHouseUuidValue.of(group.toJavaUUID()),
            ClickHouseArrayValue.of(ids.map { it.toJavaUUID() }.toTypedArray())
        )

        return ClickHouseClient.newInstance(ClickHouseProtocol.HTTP).use { client ->
            client.read(DatabaseManager.server)
                .table(DatabaseManager.eventsTable)
                .format(ClickHouseFormat.RowBinaryWithNamesAndTypes)
                .query(query.toString())
                .params(params.toTypedArray())
                .executeAndWait().use { response ->
                    val resp = response.records().map { UsageResource.fromClickhouseRecord(it) }

                    val summary = response.summary

                    println("Read: ${summary.readRows}, read total: ${summary.totalRowsToRead}")

                    resp
                }
        }
    }

    fun endUseOfResources(
        organization: UUID,
        resources: List<UsageEndResource>
    ) {
        val groupedResources = resources.groupBy { it.group }
        val resourceLookup = resources.associateBy { Pair(it.group, it.id) }

        println("-- Closing resources of ${groupedResources.keys.size} groups")
        var n = 0
        groupedResources.forEach { (group, resourceIds) ->
            println("Select ${1 + (n++)} / ${groupedResources.keys.size} group: $group")

            val storedResources =
                measureTimedValue { selectResources(organization, group, resourceIds.map { it.id }) }.run {
                    println("Selected ${resources.size} resources: $duration")
                    value
                }

            val newResources = storedResources.map {
                val usageEnd = resourceLookup[Pair(it.group, it.id)]!!
                it.copy(end = usageEnd.end)
            }

            insertUsages(newResources)
        }
    }
}

fun main() {
    println("Creating resources...")
    val organization = UUID.generateUUID()

    val resources = ArrayList<UsageResource>()

    repeat(10) {
        val group = UUID.generateUUID()

        resources.addAll((1..1000).map {
            UsageResource(
                organization,
                group,
                UUID.generateUUID(),
                "EmailAccount",
                Clock.System.now(),
                null
            )
        })
    }

    println("Inserting resources...")
    ResourceService.insertUsages(emptyList())
    measureTime {
        ResourceService.insertUsages(resources)
    }.also { println("Inserted ${resources.size} resources: $it") }

    while (resources.isNotEmpty()) {
        val resourcesToClose = (1..Random.nextInt(1, min(1000, 1 + resources.size))).map {
            resources.random().also { resources.remove(it) }
        }.map { UsageEndResource(it.group, it.id, Clock.System.now()) }

        println("Closing ${resourcesToClose.size} resources...")
        measureTime {
            ResourceService.endUseOfResources(organization, resourcesToClose)
        }.also { println("Closed ${resourcesToClose.size} resources: $it") }

        Thread.sleep(Random.nextLong(500, 1500))
    }
}
