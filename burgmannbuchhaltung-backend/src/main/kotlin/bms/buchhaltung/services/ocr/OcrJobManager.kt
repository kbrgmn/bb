package bms.buchhaltung.services.ocr

import bms.buchhaltung.services.ocr.imports.ReceiptImportParsedFile
import bms.buchhaltung.services.ocr.imports.ReceiptImportSessionState
import bms.buchhaltung.db.model.receiptimport.ImportFiles
import bms.buchhaltung.utils.json.SerializableBigDecimal
import bms.ocr.OcrManager
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.uuid.UUID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.money.Money
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.io.path.Path

object OcrJobManager {

    data class FileJob(val sessionId: String, val uuid: String, val fileName: String)

    private val queue = Channel<Job>(Channel.UNLIMITED)
    private val scope = CoroutineScope(EmptyCoroutineContext)

    init {
        scope.launch(Dispatchers.Default) {
            for (job in queue) {
                job.join()
                delay(100)
            }
        }
    }

    fun queueFile(sessionId: UUID, uuid: UUID, fileName: String) {
        println("OCR: " + Clock.System.now() + ": Queued file $fileName...")
        val job = scope.launch(EmptyCoroutineContext, CoroutineStart.LAZY) {
            ocrDocument(sessionId, uuid, fileName)
        }
        queue.trySend(job)
    }

    private suspend fun ocrDocument(sessionId: UUID, uuid: UUID, fileName: String) {
        val parsedInvoice = OcrManager.tryParseDocument(Path(fileName)).getOrNull()

        if (parsedInvoice == null) {
            transaction {
                ImportFiles.update({ (ImportFiles.importSession eq sessionId) and (ImportFiles.id eq uuid) }) {
                    it[state] = ReceiptImportSessionState.FAILURE
                    it[dateProcessed] = Clock.System.now()
                }
            }
            OcrNotificationManager.notifyUpdate(sessionId, "update-status:$uuid")
            return
        }

        fun Money.toSBD(): SerializableBigDecimal = SerializableBigDecimal(this.amount.toPlainString())

        val categories = parsedInvoice.extra.checkList.map {
            ReceiptImportParsedFile.Category(
                it.category,
                it.target.toSBD(),
                it.calculated?.toSBD(),
                it.ignored
            )
        }

        val lines = parsedInvoice.lines.map {
            ReceiptImportParsedFile.Line(it.desc, it.category, it.price?.toSBD())
        }

        val importFile = ReceiptImportParsedFile(
            categories = categories,
            lines = lines
        )

        val serializedImportFile = Json.encodeToString(importFile)

        transaction {
            ImportFiles.update({ (ImportFiles.importSession eq sessionId) and (ImportFiles.id eq uuid) }) {
                it[data] = serializedImportFile
                it[state] =
                    if (parsedInvoice.parseErrorScore() == 0) ReceiptImportSessionState.READY
                    else ReceiptImportSessionState.CORRECTION

                it[parsedTitle] = parsedInvoice.header.date.getLocalDate()
                it[title] = parsedInvoice.header.date.toGermanStringDate()
                it[dateProcessed] = Clock.System.now()
            }
        }

        OcrNotificationManager.notifyUpdate(sessionId, "update-status:$uuid")
    }
}
