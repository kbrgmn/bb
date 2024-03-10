package bms.buchhaltung.services.ocr

import bms.buchhaltung.db.model.receiptimport.ImportFiles
import bms.buchhaltung.db.model.receiptimport.ImportFiles.importSession
import bms.buchhaltung.db.model.receiptimport.ImportSessions
import bms.buchhaltung.db.model.users.Users
import bms.buchhaltung.services.ocr.imports.*
import bms.buchhaltung.utils.Crypto
import bms.buchhaltung.utils.sqlTransaction
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.uuid.UUID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.io.File
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object ReceiptImportManager {


    private val sessions = HashMap<String, ReceiptImportSession>()
    fun newSession(creator: String, stencilId: String, name: String? = null): ReceiptImportSession {

        val now = Clock.System.now()

        // TODO creator
        val sessionId = sqlTransaction {
            val creator = Users.selectAll().first()[Users.id]

            val sessionId = ImportSessions.insertAndGetId {
                it[ImportSessions.name] = name ?: "Unbenannte Sitzung vom $now"
                it[ImportSessions.stencilId] = stencilId
                it[dateCreated] = now
                it[dateEdited] = null
                it[imported] = null
                it[ImportSessions.creator] = creator
            }.value.toString()

            sessionId
        }


        val session = ReceiptImportSession(
            id = sessionId, creationDate = now, stencilId = stencilId, files = mutableListOf()
        )

        sessions[sessionId] = session

        return session
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun submitFile(sessionId: UUID, fileData: ByteArray, originalFileName: String): ReceiptImportFileStatus {
        val fileHashBytes = Crypto.hash(fileData)
        val fileHashEncoded: String = Base64.UrlSafe.encode(fileHashBytes)

        val uuid = sqlTransaction {
            ImportFiles.insert {
                it[importSession] = sessionId
                it[originalName] = originalFileName
                it[sizeInBytes] = fileData.size
                it[fileHash] = fileHashEncoded
                it[state] = ReceiptImportSessionState.PROCESSING

                it[title] = null
                it[data] = null

                it[dateCreated] = Clock.System.now()
                it[dateEdited] = null
                it[dateProcessed] = null
            }[ImportFiles.id]
        }

        val fileName = "uploads/$sessionId/$uuid.pdf"

        File(fileName).writeBytes(fileData)

        OcrJobManager.queueFile(sessionId, uuid, fileName)

        return ReceiptImportFileStatus(
            id = uuid.toString(),
            state = ReceiptImportSessionState.PROCESSING,
            fileName = originalFileName,
            title = null,
            parsedTitle = null,
            edited = false
        )
    }

    fun getFile(sessionId: UUID, fileId: UUID): ReceiptImportFileStatus = sqlTransaction {
        val row = ImportFiles.select { (importSession eq sessionId) and (ImportFiles.id eq fileId) }
            .firstOrNull() ?: throw IllegalStateException("No result found: SessionId \"${sessionId}\", FileId \"$fileId\"")

        ReceiptImportFileStatus(row)
    }

    fun getFileContent(sessionId: UUID, fileId: UUID): Result<ReceiptImportParsedFile> = runCatching {
        Json.decodeFromString<ReceiptImportParsedFile>(sqlTransaction {
            ImportFiles.select { (importSession eq sessionId) and (ImportFiles.id eq fileId) }.first()[ImportFiles.data]
        } ?: throw IllegalArgumentException("File $fileId in session $sessionId does not have data yet."))
    }

    fun getAllSessionFiles(sessionId: UUID): List<Pair<ReceiptImportFileStatus, ReceiptImportParsedFile>> =
        sqlTransaction {
            ImportFiles.select { importSession eq sessionId }.filter { it[ImportFiles.data] != null }.map {
                Pair(ReceiptImportFileStatus(it), Json.decodeFromString(it[ImportFiles.data]!!))
            }
        }


    fun updateFile(sessionId: UUID, fileId: UUID, data: String) {
        sqlTransaction {
            ImportFiles.update({ (importSession eq sessionId) and (ImportFiles.id eq fileId) }) {
                it[ImportFiles.data] = data
                it[dateEdited] = Clock.System.now()
            }
        }
    }

    fun deleteFile(sessionId: UUID, fileId: UUID) {
        sqlTransaction {
            ImportFiles.deleteWhere { (importSession eq sessionId) and (id eq fileId) }
        }
    }

    fun deleteSession(sessionId: UUID) {
        sqlTransaction {
            ImportFiles.deleteWhere { importSession eq sessionId }
            ImportSessions.deleteWhere { id eq sessionId }
        }
    }

    fun getSession(sessionId: UUID) = sqlTransaction {
        val session = ImportSessions.select { ImportSessions.id eq sessionId }.first()

        val receipts = ImportFiles.select { importSession eq sessionId }.map {
            ReceiptImportFileStatus(it)
        }

        ReceiptImportSession(
            id = session[ImportSessions.id].value.toString(),
            creationDate = session[ImportSessions.dateCreated],
            stencilId = session[ImportSessions.stencilId],
            files = receipts
        )
    }

    fun getSessionComputed(id: UUID) =
        sqlTransaction { getSessionComputedByRows(ImportSessions.select { ImportSessions.id eq id }.toList()).first() }

    fun getSessionComputedByRows(importSessions: List<ResultRow>) =
        importSessions.map {

            val receipts = sqlTransaction {
                ImportFiles.select { importSession eq it[ImportSessions.id] }
                    .map { file ->
                        ReceiptImportFileStatus(file)
                    }
            }

            val minDate = receipts.filter { it.parsedTitle != null }.minByOrNull { it.parsedTitle!! }?.parsedTitle
            val maxDate = receipts.filter { it.parsedTitle != null }.maxByOrNull { it.parsedTitle!! }?.parsedTitle

            ReceiptImportSessionComputed(
                id = it[ImportSessions.id].value.toString(),
                name = it[ImportSessions.name],
                creationDate = it[ImportSessions.dateCreated],
                editedDate = it[ImportSessions.dateEdited],
                stencilId = it[ImportSessions.stencilId],
                receiptCount = receipts.size,
                minDate = minDate,
                maxDate = maxDate,
                status = when {
                    receipts.isEmpty() -> ReceiptImportSessionState.EMPTY
                    receipts.any { it.state == ReceiptImportSessionState.PROCESSING } -> ReceiptImportSessionState.PROCESSING
                    receipts.all { it.state == ReceiptImportSessionState.FAILURE } -> ReceiptImportSessionState.FAILURE
                    receipts.any { it.state == ReceiptImportSessionState.CORRECTION || it.state == ReceiptImportSessionState.FAILURE } -> ReceiptImportSessionState.CORRECTION
                    it[ImportSessions.imported] == true -> ReceiptImportSessionState.IMPORTED
                    else -> ReceiptImportSessionState.READY
                }
            )
        }
}
