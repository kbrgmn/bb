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
import kotlinx.datetime.Clock
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import kotlinx.uuid.toJavaUUID
import java.util.concurrent.CompletableFuture
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.measureTime

object ResourceService {


    fun insertUsages(
        resources: List<UsageResource>,
        wait: Boolean = false
    ): ClickHouseResponseSummary {
        ClickHouseClient.newInstance(DatabaseManager.server.protocol).use { client ->
            val request = client.read(DatabaseManager.server)
                .write()
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

suspend fun main() {
    println("Creating resources...")

    /*measureTime {
        coroutineScope {
            repeat(100) {
                coroutineScope {
                    repeat(1000) {
                        launch {
                            ResourceService.insertUsages(
                                listOf(
                                    UsageResource(
                                        UUID.generateUUID(),
                                        UUID.generateUUID(),
                                        UUID.generateUUID(),
                                        "a",
                                        Clock.System.now(),
                                        null
                                    )
                                )
                            )
                        }
                    }
                }

                println("$it%")
            }
        }
    }.also { println("Creation for 100k took: $it") }*/

    val resources = ArrayList<UsageResource>()
    val organization = UUID.generateUUID()

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
    measureTime {
        ResourceService.insertUsages(emptyList())
    }.also { println("EMPTY INSERT: $it") }

    measureTime {
        measureTime {
            ResourceService.insertUsages(resources, true)
        }.also { println("Inserted ${resources.size} resources: $it") }

        while (resources.isNotEmpty()) {
            val resourcesToClose = (1..Random.nextInt(1, min(1000, 1 + resources.size))).map {
                resources.random().also { resources.remove(it) }
            }.map { UsageEndResource(it.group, it.id, Clock.System.now()) }

            println("Closing ${resourcesToClose.size} resources...")
            measureTime {
                ResourceService.endUseOfResources(organization, resourcesToClose)
            }.also { println("Closed ${resourcesToClose.size} resources: $it") }
        }
    }.also { println("Max age: $it") }
}
