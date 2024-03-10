package bms.buchhaltung.services.usagebilling

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlinx.uuid.UUID
import java.time.Instant

object UsageBillingService {

    val jwtSecret = "eiMeomeeBeu8eePohshohBeemidei0doopaa"
    fun createApiToken(organizationId: UUID, projectId: UUID? = null): String {
        val token = JWT.create()
            .withSubject(organizationId.toString())
            .apply {
                if (projectId != null) {
                    withClaim("project", projectId.toString())
                }
            }
            .withIssuedAt(Instant.now())
            .withIssuer("BBB") // burgmann buchhaltung backend
            .withAudience("UBS") // usage billing service
            //.withJWTId(UUID.generateUUID().toString())
            .sign(Algorithm.HMAC256(jwtSecret))
        return token
    }

}

fun main() {
    println(
        UsageBillingService.createApiToken(
            UUID("e1a2c0be-82e3-4e58-bd97-3c133e6d2fdc"),
            UUID("e1a2c0be-82e3-4e58-bd97-3c133e6d2fdc")
        )
    )
    println(
        UsageBillingService.createApiToken(
            UUID("e1a2c0be-82e3-4e58-bd97-3c133e6d2fdc"),
            null
        )
    )
}
