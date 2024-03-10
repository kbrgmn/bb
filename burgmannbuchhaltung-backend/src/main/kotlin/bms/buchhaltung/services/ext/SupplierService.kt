package bms.buchhaltung.services.ext

import bms.buchhaltung.db.model.ext.Address
import bms.buchhaltung.db.model.ext.ContactPerson
import bms.buchhaltung.db.model.ext.suppliers.Supplier
import bms.buchhaltung.db.model.ext.suppliers.Suppliers
import bms.buchhaltung.services.organizations.OrganizationService
import bms.buchhaltung.services.users.UserService
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

object SupplierService {

    fun initSchemas() {
        SchemaUtils.drop(Suppliers)
        SchemaUtils.create(Suppliers)

        val uid = UserService.listAllUsers().first().id
        val orgId = OrganizationService.getUserOrganizations(uid).first().id

        listOf(
            Supplier(organization = orgId, name = "Mann des Musters GmbH", country = "AT", address = Address("Hauptstraße 1", "1234", "Musterdorf")),
            Supplier(organization = orgId, name = "Erste Österreichische Musterfirma GmbH", country = "AT", contact = ContactPerson("Max Mustermann", "max@muster.mann", "+123456789")),
            Supplier(organization = orgId, name = "Eberhart Karl Muster e.U.", country = "AT"),
            Supplier(organization = orgId, name = "Markus Muster AG", country = "AT"),
            Supplier(organization = orgId, name = "Bernd Beispiel OG", country = "AT"),
            Supplier(organization = orgId, name = "Zulu Zum Beispiel OG", country = "AT"),
            Supplier(organization = orgId, name = "Elfriede Example KG", country = "AT"),
            Supplier(organization = orgId, name = "Fegyver- és Gépgyár Kft.", country = "HU"),
            Supplier(organization = orgId, name = "Nacht und Nebel 1 AG & KGaA", country = "AT"),
            Supplier(organization = orgId, name = "Nacht und Nebel 2 AG & KGaA", country = "AT"),
            Supplier(organization = orgId, name = "Nacht und Nebel 3 AG & KGaA", country = "AT"),
            Supplier(organization = orgId, name = "Nacht und Nebel 4 AG & KGaA", country = "AT"),
            Supplier(organization = orgId, name = "Nacht und Nebel 5 AG & KGaA", country = "AT"),
            Supplier(organization = orgId, name = "Nacht und Nebel 6 UG (haftungsbeschränkt)", country = "DE"),
            Supplier(organization = orgId, name = "Mike Muster", country = "US"),
            Supplier(organization = orgId, name = "Magdalena Muster", country = "AT"),
            Supplier(organization = orgId, name = "Mark Muster", country = "AT"),
            Supplier(organization = orgId, name = "Mini Muster Gesellschaft mbH", country = "AT"),
            Supplier(organization = orgId, name = "Maximilian Muster", country = "AT"),
            Supplier(organization = orgId, name = "Muster 2020 AG", country = "AT"),
            Supplier(organization = orgId, name = "ABC XYZ", country = "AT"),
            Supplier(
                organization = orgId,
                name = "Raab-Oedenburg-Ebenfurter Eisenbahn Győr-Sopron-Ebenfurti Vasút Zrt.",
                country = "HU"
            ),
        ).forEach {
            upsert(orgId, it)
        }
    }

    fun upsert(organizationId: UUID, supplier: Supplier): UUID {
        //check(supplier.organization != UUID.NIL) { "Supplier organization is not set" }
        // organization is set explicitly in SQL

        return transaction {
            Suppliers.upsert {
                it[organization] = organizationId
                it[id] = supplier.id

                it[name] = supplier.name
                it[vat] = supplier.vat

                it[country] = supplier.country
                it[streetAddress] = supplier.address?.street
                it[zip] = supplier.address?.zip
                it[city] = supplier.address?.city

                it[contactName] = supplier.contact?.name
                it[contactEmail] = supplier.contact?.email
                it[contactPhone] = supplier.contact?.phone

                it[notes] = supplier.notes
            }
        }[Suppliers.id]
    }

    fun get(organizationId: UUID, id: UUID): Supplier? {
        val orgId = organizationId
        val supplierId = id

        val supplierListing = transaction {
            Suppliers.select { (Suppliers.organization eq orgId) and (Suppliers.id eq supplierId) }
                .firstOrNull()
        } ?: return null

        return Supplier(supplierListing)
    }

    @Serializable
    data class SupplierListing(
        val id: UUID,
        val name: String,
    )

    fun list(organizationId: UUID): List<SupplierListing> {
        val orgId = organizationId

        val supplierListings = transaction {
            Suppliers.select { Suppliers.organization eq orgId }
                .map { SupplierListing(it[Suppliers.id], it[Suppliers.name]) }
        }

        return supplierListings
    }

    fun delete(organizationId: UUID, supplierId: UUID) = transaction {
        val orgId = organizationId
        val cid = supplierId
        val count = Suppliers.deleteWhere { (organization eq orgId) and (id eq cid) }
        count > 0
    }
}
