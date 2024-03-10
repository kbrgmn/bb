package bms.buchhaltung.services.ocr.imports

import bms.buchhaltung.utils.json.SerializableBigDecimal
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ReceiptImportLine(
    val date: LocalDate,
    val debit: String,
    val credit: String,
    val amount: SerializableBigDecimal,
    val reference: String
)
