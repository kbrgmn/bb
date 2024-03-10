package bms.buchhaltung.services.ext

import bms.buchhaltung.db.model.ext.Address
import bms.buchhaltung.db.model.ext.ContactPerson
import bms.buchhaltung.db.model.ext.customers.Customer
import bms.buchhaltung.db.model.ext.customers.Customers
import bms.buchhaltung.services.organizations.OrganizationService
import bms.buchhaltung.services.users.UserService
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object CustomerService {

    fun initSchemas() {
        SchemaUtils.drop(Customers)
        SchemaUtils.create(Customers)

        val uid = UserService.listAllUsers().first().id
        val orgId = OrganizationService.getUserOrganizations(uid).first().id

        listOf(
            Customer(organization = orgId, name = "Mann des Musters GmbH", country = "AT", address = Address("Hauptstraße 1", "1234", "Musterdorf")),
            Customer(organization = orgId, name = "Erste Österreichische Musterfirma GmbH", country = "AT", contact = ContactPerson("Max Mustermann", "max@muster.mann", "+123456789")),
            Customer(organization = orgId, name = "Eberhart Karl Muster e.U.", country = "AT"),
            Customer(organization = orgId, name = "Markus Muster AG", country = "AT"),
            Customer(organization = orgId, name = "Bernd Beispiel OG", country = "AT"),
            Customer(organization = orgId, name = "Zulu Zum Beispiel OG", country = "AT"),
            Customer(organization = orgId, name = "Elfriede Example KG", country = "AT"),
            Customer(organization = orgId, name = "Fegyver- és Gépgyár Kft.", country = "HU"),
            Customer(organization = orgId, name = "Nacht und Nebel 1 AG & KGaA", country = "AT"),
            Customer(organization = orgId, name = "Nacht und Nebel 2 AG & KGaA", country = "AT"),
            Customer(organization = orgId, name = "Nacht und Nebel 3 AG & KGaA", country = "AT"),
            Customer(organization = orgId, name = "Nacht und Nebel 4 AG & KGaA", country = "AT"),
            Customer(organization = orgId, name = "Nacht und Nebel 5 AG & KGaA", country = "AT"),
            Customer(organization = orgId, name = "Nacht und Nebel 6 UG (haftungsbeschränkt)", country = "DE"),
            Customer(organization = orgId, name = "Mike Muster", country = "US"),
            Customer(organization = orgId, name = "Magdalena Muster", country = "AT"),
            Customer(organization = orgId, name = "Mark Muster", country = "AT"),
            Customer(organization = orgId, name = "Mini Muster Gesellschaft mbH", country = "AT"),
            Customer(organization = orgId, name = "Maximilian Muster", country = "AT"),
            Customer(organization = orgId, name = "Muster 2020 AG", country = "AT"),
            Customer(organization = orgId, name = "ABC XYZ", country = "AT"),
            Customer(
                organization = orgId,
                name = "Raab-Oedenburg-Ebenfurter Eisenbahn Győr-Sopron-Ebenfurti Vasút Zrt.",
                country = "HU"
            ),
        ).forEach {
            upsert(orgId, it)
        }
    }

    fun upsert(organizationId: UUID, customer: Customer): UUID {
        //check(customer.organization != UUID.NIL) { "Customer organization is not set" }
        // organization is set explicitly in SQL

        return transaction {
            Customers.upsert {
                it[organization] = organizationId
                it[id] = customer.id

                it[name] = customer.name
                it[vat] = customer.vat

                it[country] = customer.country
                it[streetAddress] = customer.address?.street
                it[zip] = customer.address?.zip
                it[city] = customer.address?.city

                it[contactName] = customer.contact?.name
                it[contactEmail] = customer.contact?.email
                it[contactPhone] = customer.contact?.phone

                it[notes] = customer.notes
            }
        }[Customers.id]
    }

    fun get(organizationId: UUID, id: UUID): Customer? {
        val orgId = organizationId
        val customerId = id

        val customerListing = transaction {
            Customers.select { (Customers.organization eq orgId) and (Customers.id eq customerId) }
                .firstOrNull()
        } ?: return null

        return Customer(customerListing)
    }

    @Serializable
    data class CustomerListing(
        val id: UUID,
        val name: String,
    )

    fun list(organizationId: UUID): List<CustomerListing> {
        val orgId = organizationId

        val customerListings = transaction {
            Customers.select { Customers.organization eq orgId }
                .map { CustomerListing(it[Customers.id], it[Customers.name]) }
        }

        return customerListings
    }

    fun delete(organizationId: UUID, customerId: UUID) = transaction {
        val orgId = organizationId
        val cid = customerId
        val count = Customers.deleteWhere { (organization eq orgId) and (id eq cid) }
        count > 0
    }
}
