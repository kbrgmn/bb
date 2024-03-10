package bms.buchhaltung.db.model.accounting

import bms.buchhaltung.db.model.organizations.Organizations
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.money.currency
import javax.money.CurrencyUnit

object Accounts : Table() {
    val organization = reference("organization", Organizations).index()
    val id = varchar("id", 16)
    val name = varchar("name", 64)
    val currency = currency("currency")
    val description = text("description").nullable()

    override val primaryKey = PrimaryKey(organization, id)
}

@Serializable
data class Account(
    var organizationId: String,
    val id: Int,
    var name: String,
    var currency: CurrencyUnit,
    var description: String?
)
