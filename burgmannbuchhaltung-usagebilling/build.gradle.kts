plugins {
    kotlin("jvm") version "1.9.22"
    id("io.ktor.plugin") version "2.3.7"
    kotlin("plugin.serialization") version "1.9.22"

    id("com.github.ben-manes.versions") version "0.48.0"
}

group = "usagebilling.bms"
version = "0.0.1"

application {
    mainClass.set("usagebilling.bms.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven(url = "https://raw.githubusercontent.com/glureau/json-schema-serialization/mvn-repo")
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:2.3.7")
    implementation("io.ktor:ktor-server-auth:2.3.7")
    implementation("io.ktor:ktor-server-sessions-jvm:2.3.7")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.7")
    implementation("io.ktor:ktor-server-auto-head-response:2.3.7")
    //implementation("io.ktor:ktor-server-double-receive:2.3.7")
    implementation("io.ktor:ktor-server-host-common-jvm:2.3.7")
    implementation("io.ktor:ktor-server-status-pages:2.3.7")
    implementation("io.ktor:ktor-server-forwarded-header:2.3.7")
    implementation("io.ktor:ktor-server-call-logging:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("io.ktor:ktor-server-cio-jvm:2.3.7")
    implementation("io.ktor:ktor-server-cors:2.3.7")
    implementation("io.ktor:ktor-server-websockets:2.3.7")

    // -- TESTING
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    // -- TESTING END

    // Ktor server external libs
    implementation("io.github.smiley4:ktor-swagger-ui:2.7.3")
    implementation("com.github.Ricky12Awesome:json-schema-serialization:0.9.9")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk9:1.7.3")

    // Kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    implementation("app.softwork:kotlinx-uuid-core:0.0.22")

    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.2")

    // DB
    implementation("com.clickhouse:clickhouse-http-client:0.5.0")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.2.3")
    implementation("org.lz4:lz4-java:1.8.0")

    // Logging
    implementation("io.github.oshai:kotlin-logging:6.0.1")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("io.ktor:ktor-server-auth-jvm:2.3.7")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:2.3.7")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.7")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.3.7")
    implementation("io.ktor:ktor-client-cio-jvm:2.3.7")

    // Tests
    testImplementation("io.ktor:ktor-server-tests-jvm")
    //testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
