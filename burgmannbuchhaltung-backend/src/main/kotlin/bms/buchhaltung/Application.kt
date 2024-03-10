package bms.buchhaltung

import bms.buchhaltung.config.EnvConfig
import bms.buchhaltung.db.Db
import bms.buchhaltung.services.ext.customers
import bms.buchhaltung.services.ext.suppliers
import bms.buchhaltung.services.ocr.receiptImport
import bms.buchhaltung.services.organizations.organizations
import bms.buchhaltung.web.authentication
import bms.buchhaltung.web.configureSecurity
import bms.buchhaltung.web.plugins.configureCORS
import bms.buchhaltung.web.plugins.configureHTTP
import bms.buchhaltung.web.plugins.configureMonitoring
import bms.buchhaltung.web.plugins.configureOpenApi
import bms.buchhaltung.web.plugins.configureRouting
import bms.buchhaltung.web.plugins.configureSerialization
import bms.buchhaltung.web.plugins.configureStatusPages
import bms.buchhaltung.web.plugins.*
import com.sksamuel.hoplite.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.javamoney.moneta.Money
import org.slf4j.bridge.SLF4JBridgeHandler
import java.util.logging.Level
import java.util.logging.Logger


@OptIn(ExperimentalHoplite::class)
fun main(args: Array<String>) {
    println("Starting application...")
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
    Logger.getLogger(Money::class.java.name).level = Level.WARNING

    println("Loading config...")
    val envConfig = ConfigLoaderBuilder.default()
        .withExplicitSealedTypes()
        .addCommandLineSource(args)
        .addEnvironmentSource()
        .addFileSource("env.conf", optional = true)
        .build()
        .loadConfigOrThrow<EnvConfig>()

    println("Connecting to database...")
    Db.connect()
    println("Initiating database...")
    Db.init()

    println("Starting webserver...")

    if (envConfig.environment == EnvConfig.Environments.DEV) {
        System.setProperty("io.ktor.development", "true")
    }

    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configurePlugins()

    authentication()
    organizations()
    receiptImport()
    customers()
    suppliers()
}

fun Application.configurePlugins() {
    configureHTTP()
    configureStatusPages()
    configureMonitoring()
    configureSerialization()
    configureRouting()

    if (developmentMode) {
        configureOpenApi()
    }

    configureCORS()

    configureSecurity()
}
