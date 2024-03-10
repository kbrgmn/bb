package bms.buchhaltung.services.users

import bms.buchhaltung.db.model.users.User
import bms.buchhaltung.db.model.users.Users
import bms.buchhaltung.utils.sqlTransaction
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

object UserService {

    fun listAllUsers(): List<User> = sqlTransaction {
        Users.selectAll().map { resultRow -> User(resultRow) }
    }

    fun createUser(
        name: String,
        email: String,
        password: String
    ) = sqlTransaction {
        Users.insert {
            it[Users.name] = name
            it[Users.email] = email
            it[Users.password] = password
        }
    }

}
