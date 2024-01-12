package bms.usagebilling.web

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import org.slf4j.event.Level

fun Application.configureHttp() {
    install(Routing)

    install(ContentNegotiation) {
        json()
    }

    install(CallLogging) {
        level = Level.INFO

        fun Map<String, List<kotlin.String>>.stripDefaultHeaders(): Map<String, List<String>> {
            return this.filterKeys {
                it !in listOf(
                    "Host",
                    "User-Agent",
                    "Accept-Language",
                    "Accept-Encoding",
                    "Referer",
                    "DNT",
                    "Connection",
                    "Cookie",
                    "Upgrade-Insecure-Requests"
                ) && !it.startsWith("Sec-")
            }
        }

        format { call ->
            "${call.response.status()}: ${call.request.httpMethod.value} - ${call.request.path()} - Headers: ${
                call.request.headers.toMap().stripDefaultHeaders()
            } in ${call.processingTimeMillis()} ms"
        }
    }

}


