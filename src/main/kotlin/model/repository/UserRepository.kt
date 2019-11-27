package ru.aqrc.project.api.model.repository

import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import ru.aqrc.project.api.model.User
import ru.aqrc.project.api.model.database.ITransactor
import ru.aqrc.project.api.model.database.UsersTable
import java.util.*

interface IUserRepository {
    suspend fun createAsync(user: User): Deferred<User>
    suspend fun findByIdAsync(userId: UUID): Deferred<User?>
}

class UserRepository(
    private val transactor: ITransactor
) : IUserRepository {

    override suspend fun createAsync(user: User): Deferred<User> = transactor.suspendedTransaction {
        UsersTable
            .insertAndGetId { row ->
                row[name] = user.name
            }
            .value
            .let { userId -> user.copy(id = userId) }
    }

    override suspend fun findByIdAsync(userId: UUID): Deferred<User?> = transactor.suspendedTransaction {
        UsersTable
            .select { UsersTable.id eq userId }
            .limit(1)
            .map(UsersTable::toModel)
            .firstOrNull()
    }
}