package bms.usagebilling.service.resources

import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.github.smiley4.ktorswaggerui.dsl.route
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.resourceBilling() {
    routing {
        authenticate {
            route("resources", {
                securitySchemeName = "jwt"
                tags = listOf("Resources")
            }) {
                resources()
            }
        }
    }
}

private fun Route.resources() {
    get("query", {

    }) {

    }

    post("open/json", {

    }) {

    }

    post("open/csv", {

    }) {

    }

    post("close/json", {

    }) {

    }
}
