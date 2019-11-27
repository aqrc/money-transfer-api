package ru.aqrc.project.api.model.database

import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import javax.sql.DataSource

interface ITransactor {
    suspend fun <T> suspendedTransaction(block: suspend Transaction.() -> T): Deferred<T>
}

class Transactor(
    private val dataSource: DataSource
) : ITransactor {
    override suspend fun <T> suspendedTransaction(block: suspend Transaction.() -> T): Deferred<T> =
        suspendedTransactionAsync(db = Database.connect(dataSource), statement = block)
}
