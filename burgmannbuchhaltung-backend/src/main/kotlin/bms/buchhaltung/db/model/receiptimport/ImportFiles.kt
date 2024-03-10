package bms.buchhaltung.db.model.receiptimport

import bms.buchhaltung.services.ocr.imports.ReceiptImportSessionState
import kotlinx.uuid.exposed.autoGenerate
import kotlinx.uuid.exposed.kotlinxUUID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ImportFiles : Table() {
    val importSession = reference("session", ImportSessions.id).index()

    val id = kotlinxUUID("fileId").autoGenerate()
    val originalName = varchar("original_name", 128)
    val sizeInBytes = integer("size_bytes")
    val fileHash = varchar("file_hash", 128).index()
    val state = enumerationByName<ReceiptImportSessionState>("state", 24)

    val parsedTitle = date("parsed_title").nullable()
    val title = varchar("title", 128).nullable()
    val data = text("data").nullable()

    val dateCreated = timestamp("date_created")
    val dateProcessed = timestamp("date_processed").nullable()
    val dateEdited = timestamp("date_edited").nullable()


    override val primaryKey = PrimaryKey(importSession, id)
}
