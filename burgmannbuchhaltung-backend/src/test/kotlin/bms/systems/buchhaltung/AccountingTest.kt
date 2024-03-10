package bms.buchhaltung

import bms.buchhaltung.services.accounting.AccountingService
import bms.buchhaltung.db.Db
import bms.buchhaltung.db.model.accounting.Accounts
import bms.buchhaltung.db.model.accounting.Entries
import bms.buchhaltung.db.model.organizations.Organization
import bms.buchhaltung.db.model.taxes.AustrianLegalForm
import bms.buchhaltung.services.organizations.OrganizationService
import bms.buchhaltung.utils.sqlTransaction
import kotlinx.uuid.UUID
import org.javamoney.moneta.Money
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.slf4j.bridge.SLF4JBridgeHandler
import java.math.BigDecimal
import java.util.logging.*
import kotlin.test.Test

class AccountingTest {
    @Test
    fun testAccounting() {
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()
        Logger.getLogger(Money::class.java.name).level = Level.WARNING

        Db.connect()
        Db.init()


        val organization: UUID = OrganizationService.createOrganization(
            Organization(
                name = "Mustermann GmbH",
                legalForm = AustrianLegalForm.Gesellschaft_mit_beschränkter_Haftung,
                country = "AT",
                zip = "1220",
                city = "Wien",
                address = "ABC 123",
                kleinunternehmer = false
            )
        )
        println("Organization: ${organization.toString()}")

        // Guthaben
        bms.buchhaltung.services.accounting.AccountingService.createAccount(
            organization = organization,
            id = "2700",
            name = "Kassa",
            currency = "EUR",
            description = "Kassa"
        )
        bms.buchhaltung.services.accounting.AccountingService.createAccount(
            organization = organization,
            id = "2800",
            name = "Bank",
            currency = "EUR",
            description = "Bankkonto"
        )

        // Lagerbestände
        bms.buchhaltung.services.accounting.AccountingService.createAccount(
            organization = organization,
            id = "0660",
            name = "BGA",
            currency = "EUR",
            description = "Betriebs- und Geschäftsausstattung"
        )
        bms.buchhaltung.services.accounting.AccountingService.createAccount(
            organization = organization,
            id = "7600",
            name = "Büromaterial",
            currency = "EUR",
            description = "Büromaterial"
        )

        // Steuern
        bms.buchhaltung.services.accounting.AccountingService.createAccount(
            organization = organization,
            id = "2500",
            name = "VSt",
            currency = "EUR",
            description = "Vorsteuer-Guthaben, welches mit dem USt-Konto für die Umsatzsteuer gegengerechnet werden kann"
        )
        bms.buchhaltung.services.accounting.AccountingService.createAccount(
            organization = organization,
            id = "3510",
            name = "USt 10%",
            currency = "EUR",
            description = "Umsatzsteuer 10%"
        )
        bms.buchhaltung.services.accounting.AccountingService.createAccount(
            organization = organization,
            id = "3520",
            name = "USt 20%",
            currency = "EUR",
            description = "Umsatzsteuer 20%"
        )

        // Erlös
        bms.buchhaltung.services.accounting.AccountingService.createAccount(
            organization = organization,
            id = "4010",
            name = "Erlös 10%",
            currency = "EUR",
            description = "Erlöse 20%"
        )
        bms.buchhaltung.services.accounting.AccountingService.createAccount(
            organization = organization,
            id = "4020",
            name = "Erlös 20%",
            currency = "EUR",
            description = "Erlöse 20%"
        )




        bms.buchhaltung.services.accounting.AccountingService.newTransfer(
            "Wir kaufen einen Kugelschreiber",
            organization = organization,
            debitTransfers = listOf(
                "7600" /* Büro*/ to BigDecimal(81.67),
                "2500" /* VST */ to BigDecimal(16.33),
            ),
            creditTransfers = listOf(
                "2800" /* Bank*/ to BigDecimal(98)
            ),
        )


        val erloes10 = "4010"
        val erloes20 = "4020"

        val ust10 = "3510"
        val ust20 = "3520"

        val bank = "2800"

        /*
             Essen 10% 50€    55€
           Anderes 20€ 100€  120€
         */

        bms.buchhaltung.services.accounting.AccountingService.newTransfer(
            "Verkauf",
            organization = organization,
            debitTransfers = listOf(
                bank to BigDecimal(175),
            ),
            creditTransfers = listOf(
                erloes10 to BigDecimal(50),
                ust10 to BigDecimal(5),

                erloes20 to BigDecimal(100),
                ust20 to BigDecimal(20),
            )

            //  175€ bank / erloes10 50€
            //            / erloes20 100€
            //            / 5€ ust10€
            //            / 20€ ust20
        )








        println("========")
        sqlTransaction {
            val res = (Accounts innerJoin Entries).slice(
                Accounts.id,
                Accounts.name,
                Entries.amountDebit.sum(),
                Entries.amountCredit.sum()
            ).selectAll().groupBy(Accounts.id)

            res.forEach {
                println("${it[Accounts.id]} ${it[Accounts.name]}: DEBIT ${it[Entries.amountDebit.sum()]} / CREDIT ${it[Entries.amountCredit.sum()]}")
            }


            /*
            AccountTable.selectAll().select Account . accountId,
            Account.name,
            SUM(Entry.amountDebit)  "debit"
            SUM(Entry.amountCredit) "credit",
            from Account
                    join Entry on Account . id = Entry . accountId
                    group by Account.id;

             */

            /*((TransferTable innerJoin EntryTable) innerJoin AccountTable)
                .selectAll().forEach {
                    println(it)
                }*/
        }
    }
}
