package bms.usagebilling.tests

import bms.usagebilling.service.events.InsertUsageEvent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.Clock
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import kotlin.time.measureTime

suspend fun main2() {
    val groupId = UUID.generateUUID()
    val apiToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJlMWEyYzBiZS04MmUzLTRlNTgtYmQ5Ny0zYzEzM2U2ZDJmZGMiLCJpYXQiOjE3MDQ2NzQzNjUsImlzcyI6IkJCQiIsImF1ZCI6IlVCUyJ9.zHXpWWjSJ8ifPVTSQqN6QGd7X8w4Yz1EZQ28FnDc7yc"

    val http = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }

        defaultRequest {
            bearerAuth(apiToken)
        }
    }


    repeat(100) { idx ->

        val t = Clock.System.now()
        val eventNames = listOf("WeatherForecast24h", "WeatherForecast7d", "WeatherForecast30d")

        val jsonValues = (0 until 1000).map { InsertUsageEvent(group = groupId, id = UUID.generateUUID(), timestamp = t, type = eventNames.random(), reference = "JSON $idx") }

        measureTime {
            http.post("http://localhost:8080/usage-billing/push/json") {
                contentType(ContentType.Application.Json)
                setBody(jsonValues)
            }
        }.also { println("${idx+1}: Pushed ${jsonValues.size} JSON entries in: $it") }

        Thread.sleep(500)

        val csvValues = "groupId;eventId;timestamp;eventName;isBillable;reference;properties" + (0..1000).map {
            "$groupId;${UUID.generateUUID()};$t;${eventNames.random()};true;CSV $idx;"
        }.joinToString("\n")

        measureTime {
            http.post("http://localhost:8080/usage-billing/push/csv") {
                contentType(ContentType.Text.CSV)
                setBody(csvValues)
            }
        }.also { println("${idx+1}: Pushed ${jsonValues.size} CSV entries in: $it") }


        Thread.sleep(500)
    }
}
