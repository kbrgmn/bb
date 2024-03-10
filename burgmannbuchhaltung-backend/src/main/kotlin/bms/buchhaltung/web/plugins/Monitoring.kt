package bms.buchhaltung.web.plugins

import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import io.ktor.util.logging.*
import org.slf4j.event.Level


internal val LOGGER = KtorSimpleLogger("WebLog")

val RequestTracePlugin = createRouteScopedPlugin("RequestTracePlugin", { }) {
    fun ApplicationCall.toUrlLogString(): String {
        val httpMethod = request.httpMethod.value
        val userAgent = request.headers["User-Agent"]?.take(14)?.split("/")?.first()

        val url = /*this.request.uri*/ url()

        return "$httpMethod $url ($userAgent)"
    }

    fun Any.toLogString(name: String) = when {
        this == NullBody || this.toString().isEmpty() -> "no $name"
        this is Collection<*> && this.isEmpty() -> "empty $name"
        this is Map<*, *> && this.isEmpty() -> "unset $name "
        else -> "$name: \"$this\""
    }

    onCall { call ->
        val contentLength = call.request.contentLength()

        val body = when {
            contentLength == 0L -> "EMPTY BODY"
            contentLength != null -> runCatching {
                call.receiveText() + " ($contentLength bytes)"
                // val byteArray = ByteArray(call.request.contentLength()?.toInt() ?: 1024)
                // call.receiveChannel().readAvailable(byteArray)
                // String(byteArray).take(256) + " ($contentLength bytes)"
            }.getOrElse { "NON TEXT (${it::class.simpleName}: ${it.message})" }

            else -> "NO CONTENT-LENGTH"
        }

        val cookies = call.request.cookies.rawCookies
        //println(call.request.headers.entries().map { "${it.key} -> ${it.value}" })

        val urlString = call.toUrlLogString()
        if (!listOf("/r/auth/session", "/subscribe/sse").any { urlString.contains(it) }) {
            LOGGER.info("REQUEST  -> ${call.toUrlLogString()}: ${body.toLogString("body")}, ${cookies.toLogString("cookies")}")
        }
    }
    onCallRespond { call, body ->
        val status = call.response.status()?.toString() ?: "Default"
        val statusNum = call.response.status()?.value ?: "DEF"

        val urlString = call.toUrlLogString()

        if (!listOf("/r/auth/session", "/subscribe/sse").any { urlString.contains(it) }) {
            LOGGER.info("RESP $statusNum -> ${call.toUrlLogString()}: ${body.toLogString("body")} ($status)")
        }
    }
}


fun Application.configureMonitoring() {
    //install(RequestTracePlugin)
    install(CallLogging) {
        level = Level.INFO
        //filter { call -> call.request.path().startsWith("/") }
    }
}
