package bms.buchhaltung.db.model.accounting

import bms.buchhaltung.db.model.organizations.Organizations
import kotlinx.uuid.exposed.KotlinxUUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Transfers : KotlinxUUIDTable() {
    val organization = reference("organization", Organizations).index()
    val description = text("description").nullable()
    val timestamp = timestamp("timestamp")
}

data class Transfer(
    val id: Int,
    val organizationId: Int,
    val description: String?,
    val entries: List<Entry>
)

