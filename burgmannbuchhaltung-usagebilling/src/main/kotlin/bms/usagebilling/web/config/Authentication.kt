package bms.usagebilling.web.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlinx.uuid.UUID

data class CallAuthentication(
    val organizationId: UUID,
    val allowedProjectId: UUID?
) {

    companion object {
        fun PipelineContext<Unit, ApplicationCall>.authorizationFromCall(): CallAuthentication {
            val auth = call.principal<JWTPrincipal>() ?: error("No JWT principal")
            val orgId = UUID(auth.subject ?: error("No orgId"))
            val projectId = auth["project"]?.let { UUID(it) }

            return CallAuthentication(orgId, projectId)
        }
    }
}

    fun Application.apiAuthentication() {
        /*   val jwtAudience = "jwt-audience"
           val jwtDomain = "https://jwt-provider-domain/"
           */

        val jwtRealm = "BurgmannBuchhaltung UsageBilling Service"
        val jwtSecret = "eiMeomeeBeu8eePohshohBeemidei0doopaa"

        authentication {
            jwt {
                realm = jwtRealm

                challenge { defaultScheme, realm ->
                    call.response.header(HttpHeaders.WWWAuthenticate, "$defaultScheme realm=\"$realm\"")
                    call.respond(HttpStatusCode.Unauthorized, "Passed API token is not valid or has expired.")
                }

                verifier(
                    JWT
                        .require(Algorithm.HMAC256(jwtSecret))
                        .withIssuer("BBB") // BurgmannBuchhaltung Backend
                        .withAudience("UBS") // UsageBilling Service
//                    .withAudience(jwtAudience)
//                    .withIssuer(jwtDomain)
                        .build()
                )
                validate { credential ->
                    //if (credential.payload.audience.contains(jwtAudience))
                    JWTPrincipal(credential.payload)
                    //else null
                }
            }
        }
    }
