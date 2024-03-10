package bms.buchhaltung.services.ocr

import bms.buchhaltung.db.model.receiptimport.ImportFiles
import bms.buchhaltung.db.model.receiptimport.ImportSessions
import bms.buchhaltung.services.ocr.imports.*
import bms.buchhaltung.utils.sqlTransaction
import bms.ocr.spreadsheet.MultiCsvGenerator
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.smiley4.ktorswaggerui.dsl.delete
import io.github.smiley4.ktorswaggerui.dsl.get
import io.github.smiley4.ktorswaggerui.dsl.post
import io.github.smiley4.ktorswaggerui.dsl.put
import io.github.smiley4.ktorswaggerui.dsl.route
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.pipeline.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.uuid.UUID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.io.File
import java.nio.charset.Charset
import kotlin.collections.set

private val log = KotlinLogging.logger { }

private fun Route.authenticatedRoutes() {
    post("start-session", {
        request {
            queryParameter<String>("stencilId")
            queryParameter<String>("name")
        }
        response { HttpStatusCode.OK to { body<ReceiptImportSession>() } }
    }) {
        val stencilId = call.request.queryParameters["stencilId"] ?: throw IllegalArgumentException("No stencil id specified.")
        val name = call.request.queryParameters["name"]

        val creator = "TODO"
        val session = ReceiptImportManager.newSession(creator, stencilId, name)
        call.respond(session)
    }
    route("{session}", {
        request {
            pathParameter<String>("session")
        }
    }) {
        get("/subscribe/sse") {
            val sessionId = getSessionId()
            //println("SSE subscription for session: $sessionId")

            call.response.cacheControl(CacheControl.NoCache(null))
            call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                val sseSubscription = SseSubscription(this, sessionId)
                OcrNotificationManager.sessions[sessionId] = sseSubscription

                sseSubscription.sendNotification("init")

                //println("waiting for events for sse subscription $sessionId")
                for (message in sseSubscription.subscriptionChannel) {
                    sseSubscription.sendNotification(message)
                }
            }
        }

        webSocket("/subscribe/ws") {
            val sessionId = getSessionId()
            println("New ws subscription for session: $sessionId")
            val thisConnection = WsSubscription(this, sessionId)
            OcrNotificationManager.sessions[sessionId] = thisConnection

            send("connection success")
            try {
                for (frame in incoming) {
                    println("recv: $frame")
                    println(frame.readBytes().toString(Charset.defaultCharset()))
                    send("recv: " + frame.readBytes().toString(Charset.defaultCharset()))
                }
            } catch (e: ClosedReceiveChannelException) {
                println("Closed ws")
                OcrNotificationManager.sessions.remove(sessionId)
            } catch (e: Throwable) {
                println("Exception in ws")
                e.printStackTrace()
            }
        }

        post("file-submit", {
            summary = "Upload file (multipart)"
        }) {
            val sessionId = getSessionId()

            val multipartData = call.receiveMultipart()

            File("uploads/$sessionId").mkdirs()

            val receiptImportEntries = mutableListOf<ReceiptImportFileStatus>()

            multipartData.forEachPart { part ->
                if (part is PartData.FileItem) {
                    if (part.contentType != ContentType.Application.Pdf) {
                        println("WARNING: INVALID CONTENT TYPE")
                    }

                    val fileName = part.originalFileName as String
                    val fileBytes = part.streamProvider().readBytes()

                    receiptImportEntries.add(
                        ReceiptImportManager.submitFile(sessionId, fileBytes, fileName)
                    )
                } else println("Not implemented: ${part::class.java.name}")
                part.dispose()
            }

            call.respond(receiptImportEntries)
        }



        get({
            summary = "Get session information"
            response { HttpStatusCode.OK to { body<ReceiptImportSession>() } }
        }) {
            val sessionId = getSessionId()
            val session = ReceiptImportManager.getSession(sessionId)
            call.respond(session)
        }

        get("computed", {
            summary = "Get session information computed"
            response { HttpStatusCode.OK to { body<ReceiptImportSessionComputed>() } }
        }) {
            val sessionId = getSessionId()
            val session = ReceiptImportManager.getSessionComputed(sessionId)
            call.respond(session)
        }

        delete({
            summary = "Delete the specified import session"
        }) {
            val sessionId = getSessionId()

            ReceiptImportManager.deleteSession(sessionId)

            call.respond(HttpStatusCode.Accepted)
        }

        post("generate", {
            summary = "Generate accounting entries (entry lines) for this session."
            response { HttpStatusCode.OK to { body<ReceiptImportGenerateResponse>() } }
        }) {
            val sessionId = getSessionId()
            val files = ReceiptImportManager.getAllSessionFiles(sessionId)
            println("Will preview files: ${files.joinToString { it.first.fileName }}")

            val computedSession = ReceiptImportManager.getSessionComputed(sessionId)

            val lines = files.map {
                val (metadata, data) = it

                val date = metadata.parsedTitle!!

                val lines = data.lines
                    .filter { it.category != null }
                    .mapNotNull {
                        val accRec = MultiCsvGenerator.comboToAccountingRecord(Pair(it.name, it.category!!), metadata.fileName)

                        if (accRec != null && it.amount != null) {
                            val (debit, credit) = accRec
                            ReceiptImportLine(date, debit, credit, it.amount, "${it.category} / ${it.name}")
                        } else {
                            null
                        }
                    }

                ReceiptImportEntries(
                    document = metadata,
                    lines = lines
                )
            }

            sqlTransaction {
                ImportSessions.update({ ImportSessions.id eq sessionId }) {
                    it[imported] = true
                }
            }

            call.respond(ReceiptImportGenerateResponse(computedSession, lines))
        }

        post("makeCsv", {
            summary = "Make CSV output of input"
            request { body<String> { description = "Lines" } }
        }) {
            val linesString = call.receiveText()

            call.respondOutputStream {
                MultiCsvGenerator.makeCsvString(linesString, this)
            }
        }

        post("import", {
            summary = "Import the generated (and possibly edited) accounting entries (entry lines)" +
                    " for this session and mark it completed."
        }) {

        }

        route("{file}", {
            request {
                pathParameter<String>("file") { description = "File id" }
            }
        }) {
            get("/pdf", {
                summary = "Get original submitted PDF"
                response { HttpStatusCode.OK to { description = "Original uploaded PDF" } }
            }) {
                val (sessionId, fileId) = getSessionAndFile()

                call.respondFile(File("uploads/$sessionId/$fileId.pdf"))
            }

            get("/content", {
                summary = "Get file (table) content"
                response { HttpStatusCode.OK to { body<ReceiptImportParsedFile>() } }
            }) {
                val (sessionId, fileId) = getSessionAndFile()

                val file = ReceiptImportManager.getFileContent(sessionId, fileId)

                when {
                    file.isSuccess -> call.respond(file.getOrThrow())
                    else -> call.respond(ReceiptImportParsedFile(emptyList(), emptyList()))
                }
            }

            get({
                summary = "Get file metadata"
                response { HttpStatusCode.OK to { body<ReceiptImportFileStatus>() } }
            }) {
                val (sessionId, fileId) = getSessionAndFile()

                val file = ReceiptImportManager.getFile(sessionId, fileId)

                call.respond(file)
            }

            put({
                summary = "Update file"
                request {
                    body<ReceiptImportParsedFile>()
                }
            }) {
                val (sessionId, fileId) = getSessionAndFile()

                val file = call.receive<ReceiptImportParsedFile>()

                val data = Json.encodeToString(file)
                ReceiptImportManager.updateFile(sessionId, fileId, data)

                val fileIsValid = file.categories.all { (it.actual == it.expected) || it.ignored }
                        && file.lines.all { it.category in file.categories.map { it.name } || it.category == null }

                sqlTransaction {
                    ImportFiles.update({ (ImportFiles.importSession eq sessionId) and (ImportFiles.id eq fileId) }) {
                        it[state] = if (fileIsValid) ReceiptImportSessionState.READY
                        else ReceiptImportSessionState.CORRECTION
                    }
                }

                call.respond(HttpStatusCode.OK)
            }

            delete({
                summary = "Delete file from session"
            }) {
                val (sessionId, fileId) = getSessionAndFile()
                ReceiptImportManager.deleteFile(sessionId, fileId)
                call.respond(HttpStatusCode.OK)
            }
        }

    }

    get("get-sessions", {
        summary = "Get all sessions"
        response { HttpStatusCode.OK to { body<List<ReceiptImportSessionComputed>>() } }
    }) {
        val sessions = sqlTransaction {
            // TODO this returns all sessions not user specific
            ReceiptImportManager.getSessionComputedByRows(ImportSessions.selectAll().toList())
        }
        call.respond(sessions)
    }
}


fun Application.receiptImport() {
    routing {

        authenticate("authenticated-session", "authenticated-bearer") {
            route("/r/org/{organization}", {
                request {
                    pathParameter<String>("organization") {
                        description = "Organization UUID"
                    }
                }
            }) {
                route("receipt-import", {
                    tags = listOf("Receipt import")
                }) {
                    authenticatedRoutes()
                }
            }
        }
    }
}

fun ApplicationCall.getCallSessionId() =
    parameters["session"]?.let { runCatching { UUID(it) }.getOrElse { throw IllegalArgumentException("Invalid session format specified: ${parameters["session"]}") } }
        ?: throw IllegalArgumentException("No session specified.")

fun PipelineContext<Unit, ApplicationCall>.getSessionId() = call.getCallSessionId()
fun PipelineContext<Unit, ApplicationCall>.getFileId() = call.parameters["file"]?.let {
    runCatching { UUID(it) }
        .getOrElse { throw IllegalArgumentException("Invalid file format specified.") }
} ?: throw IllegalArgumentException("No file session specified.")

fun DefaultWebSocketServerSession.getSessionId() = call.getCallSessionId()

fun PipelineContext<Unit, ApplicationCall>.getSessionAndFile(): Pair<UUID, UUID> {
    val session = getSessionId()
    val fileId = getFileId()

    return Pair(session, fileId)
}

/*
 * TODO:
 * - Save file with created ID
 * ID shall NOT be file-hash (wenn User sonst aus Versehen 2x selbe File hochlädt wird das ur random replaced)
 * File hash sollte verglichen werden und Warnung angezeigt wenn User 2x selbe file hochlädt
 */
