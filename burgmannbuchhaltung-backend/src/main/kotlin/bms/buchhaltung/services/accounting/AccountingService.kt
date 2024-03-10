package bms.buchhaltung.services.accounting

import bms.buchhaltung.db.model.accounting.Accounts
import bms.buchhaltung.db.model.accounting.Entries
import bms.buchhaltung.db.model.accounting.Transfers
import bms.buchhaltung.utils.BigDecimalUtils.isNotEqual
import bms.buchhaltung.utils.sqlTransaction
import kotlinx.datetime.Clock
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import java.math.BigDecimal
import javax.money.Monetary

object AccountingService {

    fun initDatabaseSchemas() {
        SchemaUtils.drop(Entries, Transfers, Accounts)
        SchemaUtils.create(Accounts, Transfers, Entries)
    }

    fun createAccount(
        organization: UUID,
        id: String,
        name: String,
        currency: String,
        description: String
    ) {
        sqlTransaction {
            Accounts.insert {
                it[Accounts.organization] = organization
                it[Accounts.name] = name
                it[Accounts.id] = id
                it[Accounts.description] = description
                it[Accounts.currency] = Monetary.getCurrency(currency)
            }
        }
    }


    data class TransferEntry(val accountId: String, val amount: BigDecimal)
    data class TransferEntryTyped(val accountId: String, val amount: BigDecimal, val type: String)


    /**
     * @throws SidesDoNotMatchException
     * @throws AccountsInDebitAndCreditException
     * @throws AccountsDuplicatedInDebitException
     * @throws AccountsDuplicatedInCreditException
     */
    private fun checkTransferPreconditions(
        debitTransfers: List<bms.buchhaltung.services.accounting.AccountingService.TransferEntry>,
        creditTransfers: List<bms.buchhaltung.services.accounting.AccountingService.TransferEntry>
    ) {
        // Ensure: Debit sum matches credit sum
        val debitTransfersSum = debitTransfers.map { it.amount }.sum()
        val creditTransfersSum = creditTransfers.map { it.amount }.sum()
        if (debitTransfersSum isNotEqual creditTransfersSum) {
            throw bms.buchhaltung.services.accounting.SidesDoNotMatchException(debitTransfersSum, creditTransfersSum)
        }

        // Ensure: Account is not in debit and credit at the same time
        val accountsInDebitAndCredit = (debitTransfers.map { it.accountId } intersect creditTransfers.map { it.accountId })
        if (accountsInDebitAndCredit.isNotEmpty()) {
            throw bms.buchhaltung.services.accounting.AccountsInDebitAndCreditException(accountsInDebitAndCredit)
        }

        // Ensure: Account is not duplicated in debit or credit
        val debitDuplicates = debitTransfers.groupingBy { it.accountId }.eachCount().filter { it.value > 1 }
        if (debitDuplicates.isNotEmpty()) {
            throw bms.buchhaltung.services.accounting.AccountsDuplicatedInDebitException(debitDuplicates.keys)
        }
        val creditDuplicates = debitTransfers.groupingBy { it.accountId }.eachCount().filter { it.value > 1 }
        if (creditDuplicates.isNotEmpty()) {
            throw bms.buchhaltung.services.accounting.AccountsDuplicatedInCreditException(creditDuplicates.keys)
        }
    }

    fun newTransfer(
        description: String,
        organization: UUID,
        debitTransfers: List<Pair<String, BigDecimal>>,
        creditTransfers: List<Pair<String, BigDecimal>>
    ) =
        bms.buchhaltung.services.accounting.AccountingService.newTypedTransfer(
            description = description,
            organization = organization,
            debitTransfers = debitTransfers.map {
                bms.buchhaltung.services.accounting.AccountingService.TransferEntry(
                    it.first,
                    it.second
                )
            },
            creditTransfers = creditTransfers.map {
                bms.buchhaltung.services.accounting.AccountingService.TransferEntry(
                    it.first,
                    it.second
                )
            },
        )

    fun newTypedTransfer(
        description: String,
        organization: UUID,
        debitTransfers: List<bms.buchhaltung.services.accounting.AccountingService.TransferEntry>,
        creditTransfers: List<bms.buchhaltung.services.accounting.AccountingService.TransferEntry>
    ) {
        /* Preconditions */
        bms.buchhaltung.services.accounting.AccountingService.checkTransferPreconditions(
            debitTransfers = debitTransfers,
            creditTransfers = creditTransfers
        )

        /* Transfer setup */
        val transferEntriesTyped =
            creditTransfers.map {
                bms.buchhaltung.services.accounting.AccountingService.TransferEntryTyped(
                    it.accountId,
                    it.amount,
                    "CREDIT"
                )
            } union debitTransfers.map {
                bms.buchhaltung.services.accounting.AccountingService.TransferEntryTyped(
                    it.accountId,
                    it.amount,
                    "DEBIT"
                )
            }

        val transferId = UUID.generateUUID()

        /* Transfer */

        sqlTransaction {
            Transfers.insert {
                it[id] = transferId
                it[timestamp] = Clock.System.now()
                it[Transfers.organization] = organization
                it[Transfers.description] = description
            }
            Entries.batchInsert(transferEntriesTyped) {
                this[Entries.transferId] = transferId
                this[Entries.accountId] = it.accountId

                if (it.type == "CREDIT")
                    this[Entries.amountCredit] = it.amount
                else
                    this[Entries.amountDebit] = it.amount
            }
        }
    }

}
