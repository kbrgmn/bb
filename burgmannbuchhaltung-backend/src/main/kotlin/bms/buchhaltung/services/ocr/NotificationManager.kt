package bms.buchhaltung.services.ocr

import io.ktor.websocket.*
import kotlinx.coroutines.channels.Channel
import java.io.Writer
import kotlinx.uuid.UUID

abstract class NotificationSubscription(val sessionId: UUID) {

    suspend fun sendNotification(message: String) {
        //println("Sending notification to $sessionId: $message")
        sendSpecificMessage(message)
    }

    internal abstract suspend fun sendSpecificMessage(message: String)

    val subscriptionChannel = Channel<String>()
}

class SseSubscription(private val writer: Writer, sessionId: UUID) : NotificationSubscription(sessionId) {
    override suspend fun sendSpecificMessage(message: String) {
        //println("Writing SSE message: $message")
        writer.write("data: $message\n\n")
        writer.flush()
    }

}

class WsSubscription(private val session: DefaultWebSocketSession, sessionId: UUID) : NotificationSubscription(sessionId) {
    override suspend fun sendSpecificMessage(message: String) {
        session.send(message)
    }
}

object OcrNotificationManager {
    suspend fun notifyUpdate(sessionId: UUID, message: String) {
        sessions[sessionId]?.subscriptionChannel?.send(message)
    }

    val sessions = HashMap<UUID, NotificationSubscription>()

    init {
        /*thread {
            while (true) {
                println("SSE sessions: ${sessions.size}")
                Thread.sleep(60000)
            }
        }*/
    }

}
