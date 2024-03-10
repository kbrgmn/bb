package bms.buchhaltung.db.model.ext

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val street: String? = null,
    val zip: String? = null,
    val city: String? = null
)
