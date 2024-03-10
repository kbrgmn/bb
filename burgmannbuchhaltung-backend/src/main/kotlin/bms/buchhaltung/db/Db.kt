package bms.buchhaltung.db

import bms.buchhaltung.services.accounting.AccountingService
import bms.buchhaltung.services.ext.CustomerService
import bms.buchhaltung.services.ext.SupplierService
import bms.buchhaltung.services.ocr.DbOcr
import bms.buchhaltung.services.organizations.OrganizationService
import bms.buchhaltung.services.users.DbUsers
import bms.buchhaltung.utils.sqlTransaction
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection

object Db {

    fun connect() {
        Database.connect("jdbc:sqlite:data/data.db", "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    }

    fun init() {
        sqlTransaction {
            DbUsers.initSchemas()
            OrganizationService.initDatabaseSchemas()
            AccountingService.initDatabaseSchemas()
            DbOcr.initSchemas()

            CustomerService.initSchemas()
            SupplierService.initSchemas()
        }
    }
}
