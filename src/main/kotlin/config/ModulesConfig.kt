package ru.aqrc.project.api.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.koin.dsl.module
import ru.aqrc.project.api.model.repository.IUserRepository
import ru.aqrc.project.api.model.repository.UserRepository
import ru.aqrc.project.api.service.IUserService
import ru.aqrc.project.api.service.UserService
import ru.aqrc.project.api.web.Router
import ru.aqrc.project.api.web.controller.IUserController
import ru.aqrc.project.api.web.controller.UserController
import javax.sql.DataSource


object ModulesConfig {
    private val router = module {
        single { Router(get()) }
    }

    private val dataModule = module {
        single<DataSource> {
            HikariConfig()
                .apply { this.jdbcUrl = getProperty("db.jdbc.url") }
                .apply { this.username = getProperty("db.username") }
                .apply { this.password = getProperty("db.password") }
                .let(::HikariDataSource)
        }
    }

    private val userModule = module {
        single<IUserController> { UserController(get()) }
        single<IUserService> { UserService(get()) }
        single<IUserRepository> { UserRepository(get()) }
    }

    val apiModules = listOf(
        dataModule,
        router,
        userModule
    )
}
