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

@Serializable
@SerialName("ApiKeyReadableInformation")
data class ApiKeyReadableInformation(
    @SerialName("API key is for organization") val organization: String,
    @SerialName("API key is locked to group") val group: String,
    @SerialName("API key was issued by") val issuer: String,
    @SerialName("API key is intended for") val audience: List<String>,
    @SerialName("API key was issued on") val issuedAt: String,
    @SerialName("API key will expire on") val expiresAt: String,
)

@Serializable
@SerialName("StatusInformation")
data class StatusInformation(
    val web: Boolean,
    @SerialName("db-cluster") val dbCluster: Boolean,
    @SerialName("db-data") val dbData: Boolean
) {
    fun allOk() = web && dbCluster && dbData
}

fun Application.misc() {
    routing {
        route("info", {
            tags = listOf("Information")
        }) {
            authenticate(optional = true) {
                get("api-key", {
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
                            group = principal["group"] ?: "None - not bound to any group",
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
                        body<StatusInformation>()
                        description = "Service self-check OK"
                    }
                    HttpStatusCode.ServiceUnavailable to {
                        body<StatusInformation>()
                        description = "Service self-check failed - Service unavailable"
                    }
                }
            }) {
                fun Result<Boolean>.isBoolTrue(): Boolean = this.getOrElse { it.printStackTrace(); false }

                val status = StatusInformation(
                    web = true,
                    dbCluster = EventService.checkDatabaseConnectionStatus().isBoolTrue(),
                    dbData = EventService.checkDatabaseTableStatus().isBoolTrue()
                )
                val httpStatus = if (status.allOk()) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable

                context.respond(httpStatus, status)
            }
        }
    }
}
