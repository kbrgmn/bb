package bms.buchhaltung.db.model.users

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import kotlinx.uuid.exposed.KotlinxUUIDTable
import org.jetbrains.exposed.sql.ResultRow

object Users : KotlinxUUIDTable() {
    val name = varchar("name", 128)
    val email = varchar("email", 128).uniqueIndex()
    val password = varchar("password", 200)

    val image = varchar("image", 256).nullable()
}

@Serializable
data class User(
    val id: UUID,
    val email: String,
    val image: String?
) {

    constructor(r: ResultRow) : this(
        id = r[Users.id].value,
        email = r[Users.email],
        image = r[Users.image]
    )
}
