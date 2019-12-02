package ru.aqrc.project.api.model.repository

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import ru.aqrc.project.api.model.Account
import ru.aqrc.project.api.model.database.AccountsTable
import ru.aqrc.project.api.model.database.ITransactor
import ru.aqrc.project.api.service.exception.EntityNotFoundException
import ru.aqrc.project.api.service.exception.NotEnoughMoneyOnBalance
import java.math.BigDecimal
import java.util.*

interface IAccountRepository {
    suspend fun createAccountAsync(userId: UUID): Deferred<Account>
    suspend fun findByIdAsync(accountId: UUID): Deferred<Account>
    suspend fun findByUserIdAsync(userId: UUID): Deferred<List<Account>>
    suspend fun increaseAmountAsync(accountId: UUID, diff: BigDecimal): Deferred<Account>
    suspend fun decreaseAmountAsync(accountId: UUID, diff: BigDecimal): Deferred<Account>
    suspend fun transferAsync(fromAccountId: UUID, toAccountId: UUID, diff: BigDecimal): Deferred<Account>
}

class AccountRepository(
    private val transactor: ITransactor
) : IAccountRepository {
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

    override suspend fun findByIdAsync(accountId: UUID): Deferred<Account> = transactor.suspendedTransaction {
        AccountsTable
            .select { AccountsTable.id eq accountId }
            .limit(1)
            .map(AccountsTable::toModel)
            .firstOrNull()
            ?: throwNotFound(accountId)
    }

    override suspend fun findByUserIdAsync(userId: UUID): Deferred<List<Account>> = transactor.suspendedTransaction {
        AccountsTable
            .select { AccountsTable.userId eq userId }
            .map(AccountsTable::toModel)
    }

    override suspend fun increaseAmountAsync(accountId: UUID, diff: BigDecimal) = transactor.suspendedTransaction {
        val account = AccountsTable
            .select { AccountsTable.id eq accountId }
            .map(AccountsTable::toModel)
            .firstOrNull()
            ?: throwNotFound(accountId)

        val newAmount = account.amount.add(diff)

        AccountsTable
            .update(where = { AccountsTable.id eq accountId }) {
                it[amount] = newAmount
            }

        account.copy(amount = newAmount)
    }

    override suspend fun decreaseAmountAsync(accountId: UUID, diff: BigDecimal) = transactor.suspendedTransaction {
        val account = findByIdAsync(accountId).await()

        val newAmount = account.amount.subtract(diff)
            .takeIf { it >= BigDecimal.ZERO }
            ?: throwNotEnoughMoneyOnBalance(accountId, diff)

        AccountsTable
            .update(where = { AccountsTable.id eq accountId }) {
                it[amount] = newAmount
            }

        account.copy(amount = newAmount)
    }

    override suspend fun transferAsync(fromAccountId: UUID, toAccountId: UUID, diff: BigDecimal) = transactor.suspendedTransaction {
        val accounts = awaitAll(
            findByIdAsync(fromAccountId),
            findByIdAsync(toAccountId)
        )

        val fromAccount = accounts[0]
        val toAccount = accounts[1]
        val newAmountFromAccount = fromAccount.amount.subtract(diff)
            .takeIf { it >= BigDecimal.ZERO }
            ?: throwNotEnoughMoneyOnBalance(fromAccountId, diff)

        val newAmountToAccount = toAccount.amount.add(diff)

        AccountsTable
            .update(where = { AccountsTable.id eq fromAccountId }) {
                it[amount] = newAmountFromAccount
            }

        AccountsTable
            .update(where = { AccountsTable.id eq toAccountId }) {
                it[amount] = newAmountToAccount
            }

        fromAccount.copy(amount = newAmountFromAccount)
    }

    private fun throwNotFound(accountId: UUID): Nothing = throw EntityNotFoundException("Account $accountId not found.")

    private fun throwNotEnoughMoneyOnBalance(accountId: UUID, diff: BigDecimal): Nothing =
        throw NotEnoughMoneyOnBalance("Account $accountId doesn't have $diff on balance.")
}
