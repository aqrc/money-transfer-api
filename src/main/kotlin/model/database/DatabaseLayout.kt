package ru.aqrc.project.api.model.database

import org.jetbrains.exposed.dao.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
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