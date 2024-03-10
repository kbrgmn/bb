package bms.buchhaltung.services.ocr.imports

import kotlinx.serialization.Serializable

@Serializable
data class ReceiptImportEntries(
    val document: ReceiptImportFileStatus,
    val lines: List<ReceiptImportLine>
)
