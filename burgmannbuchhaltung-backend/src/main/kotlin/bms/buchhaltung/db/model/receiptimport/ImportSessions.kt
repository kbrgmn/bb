package bms.buchhaltung.db.model.receiptimport

import bms.buchhaltung.db.model.users.Users
import kotlinx.uuid.exposed.KotlinxUUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ImportSessions : KotlinxUUIDTable() {
    val name = varchar("name", 128)
    val stencilId = varchar("stencil", 128)
    val dateCreated = timestamp("date_created")
    val dateEdited = timestamp("date_edited").nullable()

    /** NULL -> not tried, false -> error, true -> success */
    val imported = bool("imported").nullable()
    val creator = reference("creator", Users.id)
}
