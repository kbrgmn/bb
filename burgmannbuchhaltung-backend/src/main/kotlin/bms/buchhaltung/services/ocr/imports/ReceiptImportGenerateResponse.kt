package bms.buchhaltung.services.ocr.imports

import kotlinx.serialization.Serializable

@Serializable
data class ReceiptImportGenerateResponse(
    val session: ReceiptImportSessionComputed,
    val entries: List<ReceiptImportEntries>,
)
