package ru.aqrc.project.api.model.repository

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import ru.aqrc.project.api.model.User
import ru.aqrc.project.api.model.database.UsersTable
import java.util.*
import javax.sql.DataSource

interface IUserRepository {
    fun create(user: User): UUID
    fun findById(userId: UUID): User?
}

class UserRepository(
    private val dataSource: DataSource
) : IUserRepository {

    override fun create(user: User): UUID {
        return transaction(Database.connect(dataSource)) {
            UsersTable
                .insertAndGetId { row ->
                    row[name] = user.name
                }
                .value
        }
    }

    override fun findById(userId: UUID): User? {
        return transaction(Database.connect(dataSource)) {
            UsersTable
                .select { UsersTable.id eq userId }
                .limit(1)
                .map(UsersTable::toModel)
                .firstOrNull()
        }
    }
}