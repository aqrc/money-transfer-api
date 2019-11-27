package ru.aqrc.project.api.model.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

interface IDatabaseInitializer {
    fun initDatabase()
}

class DatabaseInitializer(
    private val dataSource: DataSource
) : IDatabaseInitializer {
    override fun initDatabase() {
        transaction(Database.connect(dataSource)) {
            SchemaUtils.create(UsersTable)
        }
    }
}