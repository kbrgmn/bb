package bms.usagebilling.db

import bms.usagebilling.service.events.EventService
import bms.usagebilling.service.events.UsageEvent
import com.clickhouse.client.ClickHouseNode
import com.clickhouse.client.http.config.ClickHouseHttpOption
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import java.util.Properties
import kotlin.random.Random
import kotlin.time.measureTime
import kotlin.time.measureTimedValue


object DatabaseManager {

    val hosts = listOf("127.0.0.1:8123")

    val db = "default"

    val eventsTable = "events"
    val resourcesTable = "resources"

    val insertProps = Properties().apply {
        setProperty(
            ClickHouseHttpOption.CUSTOM_PARAMS.key,
            "async_insert=1,wait_for_async_insert=1" // wait_for_async_insert is enabled
        )
    }

    /*val servers = ClickHouseNodes.of(
       "jdbc:ch:http://${hosts.joinToString(",")}/$db" // jdbc:ch: prefix breaks?
               //+ "?load_balancing_policy=random&health_check_interval=5000&failover=2"
   )*/

    val server = ClickHouseNode.of("http://${hosts.first()}/$db")
}


fun main() {
    /*
     orgId UUID
     projectId UUID
     eventId UUID
     timestamp Instant
     eventName String
     properties String
     */

    val orgId = UUID("e1a2c0be-82e3-4e58-bd97-3c133e6d2fdc")
    //val projectId = UUID("6d595ce8-4226-4b9c-bbdf-a1a329093ebe")

    val projectId = UUID("e1a2c0be-82e3-4e58-bd97-3c133e6d2fdc")

    val eventNames = listOf("Issuance", "Verification")

    val timeFilter = Pair(
        LocalDateTime(2024, 1, 6, 3, 29, 1, 200*1000*1000),
        LocalDateTime(2024, 1, 6, 3, 29, 1, 400*1000*1000),
    )

    EventService.listEvents(orgId, projectId, null, Pair(0, 1))
    val eventRead = measureTimedValue {
        EventService.listEvents(orgId, projectId, null, limit = Pair(0, 500), orderDescending = true)
    }
    eventRead.value.forEach {
        println(it)
    }
    println(eventRead.duration)

    repeat(100) {
        measureTime {
            val events = ArrayList<UsageEvent>()

            repeat(50) {
                events.add(
                    UsageEvent(
                        organization = orgId,
                        group = projectId,
                        id = UUID.generateUUID(),
                        timestamp = Clock.System.now(),
                        name = eventNames.random(),
                        billable = true,
                        reference = "",
                        properties = """{"issued": "value${Random.nextInt()}"}"""
                    )
                )
            }

            EventService.insertEvents(events)

        }.also {
            println("Time: $it")
        }
        Thread.sleep(75)
    }

}
