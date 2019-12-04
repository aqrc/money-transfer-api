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
    suspend fun transferAsync(fromAccountId: UUID, toAccountId: UUID, diff: BigDecimal): Deferred<Unit>
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

    override suspend fun transferAsync(fromAccountId: UUID, toAccountId: UUID, diff: BigDecimal): Deferred<Unit> {
        return transactor.suspendedTransaction {
            val accountsExist = awaitAll(
                findByIdAsync(fromAccountId),
                findByIdAsync(toAccountId)
            )

            accountsExist[0]
                .takeIf { fromAccount -> fromAccount.amount.subtract(diff) >= BigDecimal.ZERO }
                ?: throwNotEnoughMoneyOnBalance(fromAccountId, diff)

            this.exec(MoneyTransferSqlUtil.buildQuery(fromAccountId, toAccountId, diff)) ?: Unit
        }
    }

    private fun throwNotFound(accountId: UUID): Nothing = throw EntityNotFoundException("Account $accountId not found.")

    private fun throwNotEnoughMoneyOnBalance(accountId: UUID, diff: BigDecimal): Nothing =
        throw NotEnoughMoneyOnBalance("Account $accountId doesn't have $diff on balance.")

    private object MoneyTransferSqlUtil {
        fun buildQuery(fromAccountId: UUID, toAccountId: UUID, diff: BigDecimal): String {
            return Formatter().format(
                query,
                diff.toPlainString(),
                fromAccountId.toString(),
                diff.toPlainString(),
                toAccountId.toString()
            ).toString()
        }

        private const val ID_COLUMN = "ID"

        // Didn't find a better concurrency resistant solution
        private const val query = "WITH " +
                "SENDER_ACCOUNT AS (" +
                "   SELECT $ID_COLUMN, " +
                "   ${AccountsTable.USER_ID_COLUMN}, " +
                "   ${AccountsTable.AMOUNT_COLUMN}, " +
                "   ${AccountsTable.AMOUNT_COLUMN} - %s AS NEW_AMOUNT " +
                "   FROM ACCOUNTS " +
                "   WHERE $ID_COLUMN = '%s' FOR UPDATE " +
                "), " +
                "RECEIVER_ACCOUNT AS (" +
                "   SELECT $ID_COLUMN, " +
                "   ${AccountsTable.USER_ID_COLUMN}, " +
                "   ${AccountsTable.AMOUNT_COLUMN}, " +
                "   ${AccountsTable.AMOUNT_COLUMN} + %s AS NEW_AMOUNT " +
                "   FROM ACCOUNTS " +
                "   WHERE $ID_COLUMN = '%s' FOR UPDATE " +
                ") " +
                "MERGE INTO ACCOUNTS KEY(ID) VALUES " +
                "( " +
                "   (select $ID_COLUMN from RECEIVER_ACCOUNT), " +
                "   (select ${AccountsTable.USER_ID_COLUMN} from RECEIVER_ACCOUNT), " +
                "   SELECT CASE " +
                "   WHEN (SELECT NEW_AMOUNT FROM SENDER_ACCOUNT >= 0) " +
                "   THEN (SELECT NEW_AMOUNT FROM RECEIVER_ACCOUNT) " +
                "   ELSE (SELECT ${AccountsTable.AMOUNT_COLUMN} FROM RECEIVER_ACCOUNT) " +
                "   END FOR UPDATE " +
                "), " +
                "( " +
                "   (select $ID_COLUMN from SENDER_ACCOUNT), " +
                "   (select ${AccountsTable.USER_ID_COLUMN} from SENDER_ACCOUNT), " +
                "   SELECT CASE " +
                "   WHEN (SELECT NEW_AMOUNT FROM SENDER_ACCOUNT >= 0) " +
                "   THEN (SELECT NEW_AMOUNT FROM SENDER_ACCOUNT) " +
                "   ELSE (SELECT ${AccountsTable.AMOUNT_COLUMN} FROM SENDER_ACCOUNT) " +
                "   END FOR UPDATE " +
                ")"
    }
}
