package bms.buchhaltung.db.model.ext.suppliers

import bms.buchhaltung.db.model.ext.Address
import bms.buchhaltung.db.model.ext.ContactPerson
import bms.buchhaltung.db.model.organizations.Organizations
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import kotlinx.uuid.exposed.kotlinxUUID
import kotlinx.uuid.generateUUID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table


object Suppliers : Table() {
    val organization = reference("organization", Organizations)
    val id = kotlinxUUID("id")

    val name = varchar("name", 180)
    val vat = varchar("vat", 32).nullable()

    val country = varchar("country", 2)
    val streetAddress = varchar("street_address", 250).nullable()
    val zip = varchar("zip", 12).nullable()
    val city = varchar("city", 120).nullable()


    val contactName = varchar("contact_name", 128).nullable()
    val contactEmail = varchar("contact_email", 156).nullable()
    val contactPhone = varchar("contact_phone", 50).nullable()

    val notes = text("notes").nullable()

    // todo: attachments

    override val primaryKey = PrimaryKey(organization, id)

}

@Serializable
data class Supplier(
    val organization: UUID = UUID.NIL,
    val id: UUID = UUID.generateUUID(),

    val name: String,
    val vat: String? = null,

    val country: String,
    val address: Address? = Address(),

    val contact: ContactPerson? = ContactPerson(),

    val notes: String? = null,
) {
    constructor(r: ResultRow) : this(
        organization = r[Suppliers.organization].value,
        id = r[Suppliers.id],

        country = r[Suppliers.country],
        address = Address(
            street = r[Suppliers.streetAddress],
            zip = r[Suppliers.zip],
            city = r[Suppliers.city]
        ),

        name = r[Suppliers.name],
        vat = r[Suppliers.vat],

        contact = ContactPerson(
            name = r[Suppliers.contactName],
            email = r[Suppliers.contactEmail],
            phone = r[Suppliers.contactPhone]
        ),

        notes = r[Suppliers.notes],
    )
}
