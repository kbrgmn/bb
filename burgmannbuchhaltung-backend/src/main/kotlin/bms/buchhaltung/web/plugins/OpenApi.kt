package bms.buchhaltung.web.plugins

import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.ktor.server.application.*

fun Application.configureOpenApi() {
    install(SwaggerUI) {
        swagger {
            swaggerUrl = "swagger"
            forwardRoot = true
        }
        info {
            title = "BurgmannBuchhaltung API"
            version = "latest"
            description = "Interact with the BurgmannBuchhaltung backend"
        }
        server {
            url = "http://localhost:8080"
            description = "Development Server"
        }
    }
}
