package com.osucad.server.api.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.sql.Connection

enum class Isolation(val level: Int) {
    ReadUncommitted(Connection.TRANSACTION_READ_UNCOMMITTED),
    ReadCommitted(Connection.TRANSACTION_READ_COMMITTED),
    RepeatableRead(Connection.TRANSACTION_REPEATABLE_READ),
    Serializable(Connection.TRANSACTION_SERIALIZABLE),
}

suspend fun <T> dbQuery(
    db: Database? = null,
    isolation: Isolation? = null,
    readOnly: Boolean = false,
    statement: suspend Transaction.() -> T
): T {
    return newSuspendedTransaction(
        db = db,
        transactionIsolation = isolation?.level,
        readOnly = readOnly,
    ) {
        statement()
    }
}