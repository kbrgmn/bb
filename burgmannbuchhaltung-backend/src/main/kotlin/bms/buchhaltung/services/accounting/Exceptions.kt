package bms.buchhaltung.services.accounting

import java.math.BigDecimal


open class AccountingException(override val message: String): Exception(message)

class SidesDoNotMatchException(debitAmount: BigDecimal, creditAmount: BigDecimal) :
    AccountingException("Sides do not match in attempted transfer: Debit = $debitAmount, Credit = $creditAmount")

class AccountsInDebitAndCreditException(accounts: Set<String>): AccountingException("Accounts in debit and credit in attempted transfer: ${accounts.joinToString()}")


open class AccountsDuplicatedException(type: String, accounts: Set<String>) : AccountingException("Accounts duplicated in $type: ${accounts.joinToString()}")
class AccountsDuplicatedInDebitException(accounts: Set<String>): AccountsDuplicatedException("Debit", accounts)
class AccountsDuplicatedInCreditException(accounts: Set<String>): AccountsDuplicatedException("Credit", accounts)
