import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    kotlin("jvm") version "1.9.22"
    id("io.ktor.plugin") version "2.3.8"
    kotlin("plugin.serialization") version "1.9.22"

    id("com.github.ben-manes.versions") version "0.51.0"
}

group = "burgmannbuchhaltung.burgmann.systems"
version = "0.0.1"
application {
    mainClass.set("bms.buchhaltung.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    // Ktor server
    implementation("io.ktor:ktor-server-core-jvm:2.3.8")
    implementation("io.ktor:ktor-server-auth:2.3.8")
    implementation("io.ktor:ktor-server-sessions-jvm:2.3.8")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.8")
    implementation("io.ktor:ktor-server-auto-head-response:2.3.8")
    implementation("io.ktor:ktor-server-double-receive:2.3.8")
    implementation("io.ktor:ktor-server-host-common-jvm:2.3.8")
    implementation("io.ktor:ktor-server-status-pages:2.3.8")
    implementation("io.ktor:ktor-server-forwarded-header:2.3.8")
    implementation("io.ktor:ktor-server-call-logging:2.3.8")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.8")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.8")
    implementation("io.ktor:ktor-server-cio-jvm:2.3.8")
    implementation("io.ktor:ktor-server-cors:2.3.8")
    implementation("io.ktor:ktor-server-websockets:2.3.8")

    // Ktor client
    implementation("io.ktor:ktor-client-core-jvm:2.3.8")
    implementation("io.ktor:ktor-client-serialization-jvm:2.3.8")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.8")
    implementation("io.ktor:ktor-client-json-jvm:2.3.8")
    implementation("io.ktor:ktor-client-cio-jvm:2.3.8")
    implementation("io.ktor:ktor-client-logging-jvm:2.3.8")
    implementation("io.ktor:ktor-client-cio-jvm:2.3.8")

    // Ktor server external libs
    implementation("io.github.smiley4:ktor-swagger-ui:2.7.4")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk9:1.7.3")

    // Kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    implementation("app.softwork:kotlinx-uuid-core:0.0.22")
    implementation("app.softwork:kotlinx-uuid-exposed:0.0.22")

    // Cron
    implementation("com.ucasoft.kcron:kcron-common:0.10.2")
    implementation("com.ucasoft.kcron:kcron-kotlinx-datetime:0.10.2")
    implementation("com.ucasoft.kcron:kcron-core:0.10.2")

    // Cache
    implementation("io.github.reactivecircus.cache4k:cache4k:0.12.0")

    // DB
    implementation("org.jetbrains.exposed:exposed-core:0.47.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.47.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.47.0")
    implementation("org.jetbrains.exposed:exposed-money:0.47.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.47.0")
    implementation("org.jetbrains.exposed:exposed-json:0.47.0")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0") // 3.43.0.0

    // Money
    implementation("org.javamoney:moneta:1.4.4")

    // Crypto
    //implementation("com.ionspin.kotlin:multiplatform-crypto-libsodium-bindings:0.8.9")
    implementation("de.mkammerer:argon2-jvm:2.11")

    /* OCR */
    // Currency
    implementation("org.joda:joda-money:1.0.4")

    // XML
    implementation("org.jsoup:jsoup:1.17.2")


    // XLSX
    //compileOnly("com.github.dhatim:fastexcel:0.15.7")
    implementation("org.dhatim:fastexcel:0.16.6")

    // CSV
    implementation("com.github.doyaaaaaken:kotlin-csv:1.9.3")

    // Logging
    implementation("io.github.oshai:kotlin-logging:6.0.3")
    implementation("org.slf4j:slf4j-simple:2.0.12")
    implementation("org.slf4j:jul-to-slf4j:2.0.12")

    // Config
    implementation("com.sksamuel.hoplite:hoplite-core:2.8.0.RC3")
    implementation("com.sksamuel.hoplite:hoplite-hocon:2.8.0.RC3")
    implementation("io.ktor:ktor-client-encoding:2.3.8")

    // Test
    testImplementation(kotlin("test"))
    //testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.10")
    testImplementation("io.ktor:ktor-server-tests-jvm:2.3.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}


tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        listOf("beta", "alpha").any {
            candidate.version.lowercase().contains(it.lowercase())
        } || candidate.version.contains("-RC")
    }
}

/*
tasks.withType<Tar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Zip> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
tasks.withType<org.gradle.api.tasks.Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
*/
