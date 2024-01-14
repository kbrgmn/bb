package bms.usagebilling.web.config

import com.github.ricky12awesome.jss.encodeToSchema
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.data.AuthScheme
import io.github.smiley4.ktorswaggerui.data.AuthType
import io.github.smiley4.ktorswaggerui.data.EncodingData
import io.github.smiley4.ktorswaggerui.dsl.getTypeName
import io.ktor.server.application.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.io.path.Path
import kotlin.io.path.exists

fun Application.configureOpenApi() {
    install(SwaggerUI) {

        val json = Json {
            prettyPrint = true
            encodeDefaults = true
        }

        encoding {
            exampleEncoder { type, example ->
                //println("Encoding: ${type} ($example)")

                if (type?.getTypeName()?.contains("bms.usagebilling") == true) {
                    json.encodeToString(serializer(type), example)
                } else {
                    EncodingData.DEFAULT.exampleEncoder.invoke(type, example)
                }
            }
            schemaEncoder { type ->
                if (type.getTypeName().contains("bms.usagebilling")) {
                    json.encodeToSchema(serializer(type), generateDefinitions = false)
                    //.also { println("KOTLIN SCHEMA FOR: $typeName = $it") }
                } else {
                    EncodingData.DEFAULT.schemaEncoder.invoke(type)
                }
            }
            schemaDefinitionsField = "definitions"
        }

        swagger {
            swaggerUrl = "documentation"
            forwardRoot = true
        }
        info {
            title = "BurgmannBuchhaltung UsageBilling API"
            version = "latest"
            description =
                "This API allows you to interact with the BurgmannBuchhaltung UsageBilling module. " +
                        "This requires you to already have an BurgmannBuchhaltung standard account and an active license for the UsageBilling module." +
                        if (Path("/home/gatgeagent").exists()) " eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJlMWEyYzBiZS04MmUzLTRlNTgtYmQ5Ny0zYzEzM2U2ZDJmZGMiLCJpYXQiOjE3MDUwMjAxNTYsImlzcyI6IkJCQiIsImF1ZCI6IlVCUyJ9.Wg2RtTloL3CCoFbR02n6lGoBuw3rqSVwUp0VaicJi-U" else "" // FIXME
            contact {
                name = "Technical support"
                url = "https://buchhaltung.burgmann.systems/support/technical"
                email = "tech-support@buchhaltung.burgmann.systems"
            }
        }
        /*server {
            url = "http://localhost:8080"
            description = "Development Server"
        }*/

        securityScheme("jwt") {
            name = "JWT API key authentication"
            description = "Pass the JWT API key you created in BurgmannBuchhaltung API or frontend"
            scheme = AuthScheme.BEARER
            type = AuthType.HTTP
        }
    }
}
