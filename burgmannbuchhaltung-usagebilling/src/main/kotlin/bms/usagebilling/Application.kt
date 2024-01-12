package bms.usagebilling

import bms.usagebilling.web.apiAuthentication
import bms.usagebilling.web.configureHttp
import bms.usagebilling.web.configureOpenApi
import bms.usagebilling.web.exceptions
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureHttp()
    configureOpenApi()
    exceptions()
    apiAuthentication()

    misc()
    eventsBilling()
}
