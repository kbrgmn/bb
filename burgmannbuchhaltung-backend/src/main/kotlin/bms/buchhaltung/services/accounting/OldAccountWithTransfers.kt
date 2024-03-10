package bms.buchhaltung.services.accounting

import bms.buchhaltung.db.model.accounting.Account
import bms.buchhaltung.db.model.accounting.Transfer
import org.javamoney.moneta.Money
import java.math.BigDecimal
import javax.money.MonetaryAmount

data class AccountWithTransfers(
    val account: Account, val transfers: List<Transfer>
    /*
    val debit: MonetaryAmount
    val credit: MonetaryAmount,
    */
) {


    /*
    fun getAllEntries() = transfers
        .flatMap { it.entries }
        .filter { it.accountId == account.id }
     */

    /*
    fun calculateTotalDebit() = getAllEntries()
        .map { it.amountDebit }
        .sum()
    fun calculateTotalCredit() = getAllEntries()
        .map { it.amountCredit }
        .sum()
     */

}

fun List<MonetaryAmount?>.sum(): MonetaryAmount {
    var sum: MonetaryAmount = Money.of(0, "EUR")
    for (money in this) {
        if (money != null) {
            sum = sum.add(money)
        }
    }
    return sum
}

fun List<BigDecimal?>.sum(): BigDecimal {
    var sum: BigDecimal = BigDecimal.ZERO
    for (money in this) {
        if (money != null) {
            sum = sum.add(money)
        }
    }
    return sum
}
