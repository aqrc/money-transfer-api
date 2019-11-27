package ru.aqrc.project.api.model.database

import org.jetbrains.exposed.dao.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import ru.aqrc.project.api.model.Account
import ru.aqrc.project.api.model.User

object UsersTable : UUIDTable() {
    private const val NAME_COLUMN = "name"

    val name: Column<String> = varchar(NAME_COLUMN, 100)

    fun toModel(row: ResultRow): User {
        return User(
            id = row[id].value,
            name = row[name]
        )
    }
}

object AccountsTable : UUIDTable() {
    private const val USER_ID_COLUMN = "userId"
    private const val AMOUNT_COLUMN = "amount"
    private const val AMOUNT_PRECISION = 38
    const val AMOUNT_SCALE = 8

    val userId = uuid(USER_ID_COLUMN).references(UsersTable.id)
    val amount = decimal(AMOUNT_COLUMN, AMOUNT_PRECISION, AMOUNT_SCALE)

    fun toModel(row: ResultRow): Account {
        return Account(
            id = row[id].value,
            userId = row[userId],
            amount = row[amount]
        )
    }
}