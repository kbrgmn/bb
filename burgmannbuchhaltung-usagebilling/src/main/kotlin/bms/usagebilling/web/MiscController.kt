package bms.usagebilling.web

import bms.usagebilling.service.events.EventService
import bms.usagebilling.web.config.UnauthorizedException
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Serializable
@SerialName("ApiKeyReadableInformation")
data class ApiKeyReadableInformation(
    @SerialName("API key is for organization") val organization: String,
    @SerialName("API key is locked to project") val project: String,
    @SerialName("API key was issued by") val issuer: String,
    @SerialName("API key is intended for") val audience: List<String>,
    @SerialName("API key was issued on") val issuedAt: String,
    @SerialName("API key will expire on") val expiresAt: String,
)

fun Application.misc() {
    routing {
        route("", {
            tags = listOf("Information")
        }) {
            authenticate(optional = true) {
                get("api-key-info", {
                    summary = "View information about the API key you are using."
                    securitySchemeName = "jwt"

                    response { HttpStatusCode.OK to { body<ApiKeyReadableInformation>() } }
                }) {
                    val principal = call.principal<JWTPrincipal>() ?: throw UnauthorizedException(
                        "Your API key information request failed as no authorized " + "principal was retrieved from your key. It was either not found, or is invalid. " + "Please check the `Authorization` header you are sending; it should be \"Authorization: Bearer <API key here>\"."
                    )

                    call.respond(
                        ApiKeyReadableInformation(
                            organization = principal.subject!!,
                            project = principal["project"] ?: "None - not bound to any project",
                            issuer = when (principal.issuer) {
                                "BBB" -> "BurgmannBuchhaltung Primary Service"
                                else -> principal.issuer
                            } ?: "UNKNOWN",
                            audience = principal.audience.map {
                                when (it) {
                                    "UBS" -> "BurgmannBuchhaltung UsageBilling Module Service"
                                    else -> it
                                }
                            },
                            issuedAt = principal.issuedAt?.toString() ?: "UNKNOWN",
                            expiresAt = principal.expiresAt?.toString()
                                ?: "Never - this API token will not expire in the future",
                        )
                    )
                }
            }

            get("status", {
                summary = "Displays the services operational status."
                response {
                    HttpStatusCode.OK to {
                        body<Unit>()
                    }
                }
            }) {
                fun Result<Boolean>.isBoolTrue(): Boolean = this.getOrElse { it.printStackTrace(); false }

                val statuses = mapOf(
                    "web" to true,
                    "db-cluster" to EventService.checkDatabaseConnectionStatus().isBoolTrue(),
                    "db-data" to EventService.checkDatabaseTableStatus().isBoolTrue()
                )
                val status = if (statuses.values.all { it }) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable

                context.respond(status, JsonObject(statuses.mapValues { JsonPrimitive(it.value) }))
            }
        }
    }
}
