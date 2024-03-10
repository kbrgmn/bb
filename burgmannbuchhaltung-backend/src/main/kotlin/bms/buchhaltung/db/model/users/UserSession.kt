package bms.buchhaltung.db.model.users

import org.jetbrains.exposed.dao.id.IntIdTable

object UserSession : IntIdTable() {
    val user = reference("user", Users).index()

    val ip = varchar("ip", 64)
    val userAgent = varchar("user_agent", 128)

    val startDate = varchar("start_date", 64)
    val lastActiveDate = varchar("last_active_date", 64)

    val token = varchar("token", 128)
}
