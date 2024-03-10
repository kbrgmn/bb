package bms.buchhaltung.db.model.accounting

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import javax.money.MonetaryAmount

object Entries : Table() {
    val transferId = reference("transferId", Transfers.id)
    val accountId = reference("accountId", Accounts.id)

    // Soll
    val amountDebit = decimal(
        "amountDebit",
        15,
        4
    ).nullable() //compositeMoney(11, 2, "amountDebit").nullable() as CompositeMoneyColumn<BigDecimal?, CurrencyUnit?, MonetaryAmount?>

    // Haben
    val amountCredit = decimal(
        "amountCredit",
        15,
        4
    ).nullable() //compositeMoney(11, 2, "amountCredit").nullable() as CompositeMoneyColumn<BigDecimal?, CurrencyUnit?, MonetaryAmount?>
     //val type = varchar("type", 6)

    init {
        index(isUnique = false, transferId)
    }

    override val primaryKey = PrimaryKey(transferId, accountId)
}

@Serializable
data class Entry(
    val id: Int,
    val transferId: Int,
    val accountId: Int,
    val amountDebit: MonetaryAmount?,
    val amountCredit: MonetaryAmount?
)

