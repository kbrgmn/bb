package bms.buchhaltung.services.ocr.imports

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ReceiptImportSessionComputed(
    val id: String,
    val name: String,
    val creationDate: Instant,
    val editedDate: Instant?,
    val stencilId: String,
    val receiptCount: Int,
    val minDate: LocalDate?,
    val maxDate: LocalDate?,
    val status: ReceiptImportSessionState
)
