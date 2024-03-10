package bms.buchhaltung.config

import kotlinx.serialization.Serializable

@Serializable
data class EnvConfig(
    val environment: Environments = Environments.DEV,

    ) {
    @Serializable
    enum class Environments {
        PRODUCTION,
        DEV
    }
}
