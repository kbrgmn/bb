package bms.buchhaltung.db.model.ext

import kotlinx.serialization.Serializable

@Serializable
data class ContactPerson(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
)
