package ru.aqrc.project.api.model.repository

import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import ru.aqrc.project.api.model.Account
import ru.aqrc.project.api.model.database.AccountsTable
import ru.aqrc.project.api.model.database.ITransactor
import java.math.BigDecimal
import java.util.*

interface IAccountRepository {
    suspend fun createAccountAsync(userId: UUID): Deferred<Account>
    suspend fun findByIdAsync(accountId: UUID): Deferred<Account?>
}

class AccountRepository(
    private val transactor: ITransactor
): IAccountRepository {
    private companion object {
        val BIG_DECIMAL_ZERO: BigDecimal = BigDecimal.valueOf(0, AccountsTable.AMOUNT_SCALE)
    }

    override suspend fun createAccountAsync(userId: UUID): Deferred<Account> = transactor.suspendedTransaction {
        AccountsTable
            .insertAndGetId { row ->
                row[this.userId] = userId
                row[amount] = BIG_DECIMAL_ZERO
            }
            .value
            .let { accountId ->
                Account(
                    id = accountId,
                    userId = userId,
                    amount = BIG_DECIMAL_ZERO
                )
            }
    }

    override suspend fun findByIdAsync(accountId: UUID): Deferred<Account?> = transactor.suspendedTransaction {
        AccountsTable
            .select { AccountsTable.id eq accountId }
            .limit(1)
            .map(AccountsTable::toModel)
            .firstOrNull()
    }
}
