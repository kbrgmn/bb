package bms.buchhaltung.services.ocr.imports

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ReceiptImportSession(
    val id: String,
    val creationDate: Instant,
    val stencilId: String,
    val files: List<ReceiptImportFileStatus>
)
