package bms.buchhaltung.services.ocr.imports

import kotlinx.serialization.Serializable

@Serializable
enum class ReceiptImportSessionState {
    EMPTY,
    PROCESSING,
    /** correction = invalid (at least 1 error) */
    CORRECTION,
    FAILURE,
    READY,
    GENERATED,
    IMPORTED
}
