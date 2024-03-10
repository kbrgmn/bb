package bms.buchhaltung.db.model.invoicereference

import bms.buchhaltung.db.model.accounting.Transfers
import org.jetbrains.exposed.sql.Table

object InvoiceReferences : Table() {
    val transfer = reference("transfer", Transfers.id).index()
    val referencedInvoices = reference("transfer", ReferencedInvoices.id)

    override val primaryKey = PrimaryKey(transfer, referencedInvoices)
}
