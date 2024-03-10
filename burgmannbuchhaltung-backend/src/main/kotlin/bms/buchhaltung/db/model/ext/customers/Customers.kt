package bms.buchhaltung.db.model.ext.customers

import bms.buchhaltung.db.model.ext.Address
import bms.buchhaltung.db.model.ext.ContactPerson
import bms.buchhaltung.db.model.organizations.Organizations
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import kotlinx.uuid.exposed.kotlinxUUID
import kotlinx.uuid.generateUUID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object Customers : Table() {
    val organization = reference("organization", Organizations)
    val id = kotlinxUUID("id")

    val privateIndividual = bool("private_individual").default(false) // TODO

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
data class Customer(
    val organization: UUID = UUID.NIL,
    val id: UUID = UUID.generateUUID(),

    val privateIndividual: Boolean = false,

    val name: String,
    val vat: String? = null,

    val country: String,
    val address: Address? = Address(),

    val contact: ContactPerson? = ContactPerson(),

    val notes: String? = null,
) {

    constructor(r: ResultRow) : this(
        organization = r[Customers.organization].value,
        id = r[Customers.id],

        privateIndividual = r[Customers.privateIndividual],

        country = r[Customers.country],
        address = Address(
            street = r[Customers.streetAddress],
            zip = r[Customers.zip],
            city = r[Customers.city]
        ),

        name = r[Customers.name],
        vat = r[Customers.vat],

        contact = ContactPerson(
            name = r[Customers.contactName],
            email = r[Customers.contactEmail],
            phone = r[Customers.contactPhone]
        ),

        notes = r[Customers.notes],
    )
}
