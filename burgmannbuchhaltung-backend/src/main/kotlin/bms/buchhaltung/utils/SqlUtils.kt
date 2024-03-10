package bms.buchhaltung.utils

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transactionManager

fun <T> sqlTransaction(db: Database? = null, statement: Transaction.() -> T): T =
    org.jetbrains.exposed.sql.transactions.transaction(
        transactionIsolation = db.transactionManager.defaultIsolationLevel,
        readOnly = db.transactionManager.defaultReadOnly,
        db = db
    ) {
        // ...addLogger...
        statement.invoke(this)
    }


