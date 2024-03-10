package bms.buchhaltung.services.ocr.imports

import bms.buchhaltung.db.model.receiptimport.ImportFiles
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

@Serializable
data class ReceiptImportFileStatus(
    val id: String,
    val state: ReceiptImportSessionState,
    val fileName: String,
    val title: String?,
    val parsedTitle: LocalDate?,
    val edited: Boolean
) {

    constructor(row: ResultRow) : this(
        id = row[ImportFiles.id].toString(),
        state = row[ImportFiles.state],
        fileName = row[ImportFiles.originalName],
        title = row[ImportFiles.title],
        parsedTitle = row[ImportFiles.parsedTitle],
        edited = row[ImportFiles.dateEdited] != null
    )

}

