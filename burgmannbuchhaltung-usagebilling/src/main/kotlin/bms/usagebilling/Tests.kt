package bms.usagebilling

import bms.usagebilling.service.resources.ResourceService
import bms.usagebilling.service.resources.UsageEndResource
import bms.usagebilling.service.resources.UsageResource
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

object Tests {

    suspend fun testLotsOfSingleResourceInserts() {
        println("Creating resources...")

        measureTime {
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
        }.also { println("Creation for 100k took: $it") }
    }

    fun testResourceClosing() {

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

    fun lotsOfResources() = measureTime {
        val org = UUID.generateUUID()
        val group = UUID.generateUUID()

        val c = Clock.System

        val resources = measureTimedValue {
            (1..1_000_000).map {
                UsageResource(org, group, UUID.generateUUID(), "Weather", c.now(), null)
            }
        }.also { println("Generating resources took: ${it.duration}") }

        measureTime {
            ResourceService.insertUsages(resources.value).also {
                println(
                    """
                Inserted: ${resources.value.size} resources
                ${it.readRows} rows read
                ${it.totalRowsToRead} rows read total
                ${it.readBytes} bytes read
                ${it.writtenBytes} bytes written
                ${it.writtenRows} rows written
            """.trimIndent()
                )
            }
        }.also {
            println("Inserting: $it")
        }
    }.also { println("Took: $it in total") }
}

@Suppress("RedundantSuspendModifier")
suspend fun main() {
    // put tests here
}
