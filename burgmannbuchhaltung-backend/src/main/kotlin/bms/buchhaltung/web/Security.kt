package bms.buchhaltung.web

import bms.buchhaltung.db.model.users.Users
import bms.buchhaltung.services.organizations.OrganizationService
import bms.buchhaltung.utils.RandomUtils
import de.mkammerer.argon2.Argon2Factory
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.github.smiley4.ktorswaggerui.dsl.route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.uuid.UUID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.collections.set
import kotlin.time.Duration.Companion.days

private val log = KotlinLogging.logger { }

@Serializable
data class LoginRequest(val username: String, val password: String) {
    //constructor(email: String, password: String) : this(email, password.toCharArray())
}

@Suppress("ArrayInDataClass")
data class ByteLoginRequest(val username: String, val password: ByteArray) {
    constructor(loginRequest: LoginRequest) : this(loginRequest.username, loginRequest.password.toByteArray())

    override fun toString() = "[LOGIN REQUEST FOR: $username]"
}

private fun generateToken() = RandomUtils.randomBase64UrlString(256)

data class LoginTokenSession(val token: String) : Principal

fun Application.configureSecurity() {

    install(Sessions) {
        val encryptionKey = "laephohF2hoh8coc".toByteArray()
        val signKey = "iePaipais8ohttou".toByteArray()

        cookie<LoginTokenSession>("login") {
            //cookie.encoding = CookieEncoding.BASE64_ENCODING

            //cookie.httpOnly = true
            cookie.httpOnly = false // FIXME
            // TODO cookie.secure = true
            cookie.maxAge = 1.days
            cookie.extensions["SameSite"] = "Strict"
            transform(SessionTransportTransformerEncrypt(encryptionKey, signKey))
        }
    }

    install(Authentication) {

        bearer {
            bearer("authenticated-bearer") {
                authenticate { tokenCredential ->
                    if (securityUserTokenMapping.contains(tokenCredential.token)) {
                        UserIdPrincipal(securityUserTokenMapping[tokenCredential.token].toString())
                    } else {
                        null
                    }
                }
            }
        }

        session<LoginTokenSession>("authenticated-session") {
            validate { session ->
                //println("Validating: $session, [$securityUserTokenMapping]")
                if (securityUserTokenMapping.contains(session.token)) {
                    UserIdPrincipal(securityUserTokenMapping[session.token].toString())
                } else {
                    sessions.clear("login")
                    null
                }
            }

            challenge {
                call.respond(HttpStatusCode.Unauthorized, "Login to continue.")
            }
        }
    }
}


val securityUserTokenMapping = HashMap<String, kotlinx.uuid.UUID>()
val tokenEmailMapping = HashMap<String, String>()


fun Application.authentication() {
    routing {
        route("r/auth", {
            tags = listOf("auth")
        }) {
            post("login", {
                summary = "Login as user"
                description = "Login with email + password"
                request {
                    body<LoginRequest> {
                        example("example", LoginRequest("string@string.string", "string"))
                    }
                }
                response {
                    HttpStatusCode.Accepted to {
                        description = "Login successful"
                    }
                    HttpStatusCode.Unauthorized to {
                        description = "Login failed"
                    }
                }
            }) {
                println("Login request")
                //val loginReq = ByteLoginRequest(call.receive<LoginRequest>())
                val reqBody = call.receive<JsonObject>()
                val loginReq = ByteLoginRequest(
                    username = reqBody["username"]!!.jsonPrimitive.content,
                    password = reqBody["password"]!!.jsonPrimitive.content.toByteArray()
                )

                val query = Users.select { Users.email eq loginReq.username }
                val hasUser = transaction { query.count() > 0 }

                if (!hasUser)
                    throw UnauthorizedException("Unknown user \"${loginReq.username}\".")

                val user = transaction { query.first() }
                val pwHash = user[Users.password]

                val passwordMatches = Argon2Factory.create().run {
                    verify(pwHash, loginReq.password).also {
                        wipeArray(loginReq.password)
                    }
                }

                if (passwordMatches) {
                    val id = user[Users.id].value
                    val token = generateToken()
                    securityUserTokenMapping[token] = id
                    tokenEmailMapping[token] = user[Users.email]
                    call.sessions.set(LoginTokenSession(token))

                    //call.respond(HttpStatusCode.Accepted)
                    //call.respond(mapOf("token" to mapOf("accessToken" to token)))
                    call.respond(mapOf("token" to token))
                } else {
                    throw UnauthorizedException("Invalid password for \"${loginReq.username}\"!")
                }
            }

            authenticate("authenticated-session", "authenticated-bearer") {
                get("user-info", {
                    summary = "Return user name"
                }) {
                    call.respond(getUserId())
                }
                get("session", {
                    summary = "Check if session is still valid"
                }) {
                    //val token = getUserId().name
                    val token =
                        call.sessions.get(LoginTokenSession::class)?.token ?: call.request.authorization()
                            ?.removePrefix("Bearer ")
                        ?: throw UnauthorizedException("Invalid session")

                    @Serializable
                    data class SessionResponse(val token: Map<String, String>, val email: String)

                    if (securityUserTokenMapping.contains(token)) {
                        val resp = SessionResponse(mapOf("accessToken" to token), tokenEmailMapping[token] ?: "")

                        call.respond(resp)
                    } else throw UnauthorizedException("Invalid (outdated?) session!")
                }
            }

            post("logout", {
                summary = "Logout as user"
                response { HttpStatusCode.OK to { description = "Logged out." } }
            }) {
                call.sessions.clear<LoginTokenSession>()
                call.respond(HttpStatusCode.OK)
            }
        }

        route("user-management") {
            post("create", {
                request { body<LoginRequest>() }
            }) {
                val req = ByteLoginRequest(call.receive<LoginRequest>())

                val hash = Argon2Factory.create().run {
                    hash(10, 65536, 1, req.password).also {
                        wipeArray(req.password)
                    }
                }

                transaction {
                    println("Inserting: ${req.username}, $hash")
                    Users.insert {
                        it[name] = "Kevin Burgmann"
                        it[email] = req.username
                        it[password] = hash
                    }
                }

                call.respond(HttpStatusCode.Created)
            }
        }
    }
}


fun PipelineContext<Unit, ApplicationCall>.getUserId() = (
        call.principal<UserIdPrincipal>("authenticated-session")
            ?: call.principal<UserIdPrincipal>("authenticated-bearer")
            ?: throw UnauthorizedException("Could not retrieve authorized user.")).name

fun PipelineContext<Unit, ApplicationCall>.getUserUUID() =
    runCatching { UUID(getUserId()) }.getOrElse { e ->
        e.printStackTrace()
        throw IllegalArgumentException("Invalid user id: $e")
    }


fun PipelineContext<Unit, ApplicationCall>.getRoleInOrganization(): String? {
    val organization =
        UUID(call.parameters["organization"] ?: throw IllegalArgumentException("No organization specified!"))
    return OrganizationService.getUserRoleInOrganization(organization, getUserUUID())
}

fun PipelineContext<Unit, ApplicationCall>.getOrganizationId(): UUID {
    val organization =
        UUID(call.parameters["organization"] ?: throw IllegalArgumentException("No organization specified!"))
    if (getRoleInOrganization() == null) {
        throw IllegalArgumentException("User is not part of organization!")
    }
    return organization
}
