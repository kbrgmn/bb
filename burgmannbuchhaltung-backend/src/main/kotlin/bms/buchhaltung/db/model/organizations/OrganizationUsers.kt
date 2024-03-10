package bms.buchhaltung.db.model.organizations

import bms.buchhaltung.db.model.users.Users
import org.jetbrains.exposed.sql.Table

object OrganizationUsers : Table() {
    val organization = reference("organization", Organizations.id)
    val user = reference("user", Users.id)

    val role = varchar("role", 64)

    override val primaryKey = PrimaryKey(organization, user)

}
