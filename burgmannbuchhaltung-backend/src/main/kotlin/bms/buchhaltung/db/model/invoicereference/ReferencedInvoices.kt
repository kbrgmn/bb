package bms.buchhaltung.db.model.invoicereference

import kotlinx.uuid.exposed.KotlinxUUIDTable

object ReferencedInvoices : KotlinxUUIDTable() {
    val file = varchar("file", 256)
}
