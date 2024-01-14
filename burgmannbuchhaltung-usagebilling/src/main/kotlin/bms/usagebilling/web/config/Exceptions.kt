package bms.usagebilling.web.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

data class UnauthorizedException(
    override val message: String
) : WebException(HttpStatusCode.Unauthorized, message)

open class WebException(val statusCode: HttpStatusCode, override val message: String) : Exception(message)

fun Application.exceptions() {

    install(StatusPages) {
        exception<WebException> { call, cause ->
            call.respond(cause.statusCode, cause.message)
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, "${cause::class.simpleName}: " + cause.message)
        }
        exception<RuntimeException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, "${cause::class.simpleName}: " + cause.message)
        }
    }
}

fun illegalArgument(message: String): Nothing = throw IllegalArgumentException(message)
