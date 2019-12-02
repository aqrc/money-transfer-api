package ru.aqrc.project.api.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.koin.dsl.module
import ru.aqrc.project.api.model.database.DatabaseInitializer
import ru.aqrc.project.api.model.database.IDatabaseInitializer
import ru.aqrc.project.api.model.database.ITransactor
import ru.aqrc.project.api.model.database.Transactor
import ru.aqrc.project.api.model.repository.AccountRepository
import ru.aqrc.project.api.model.repository.IAccountRepository
import ru.aqrc.project.api.model.repository.IUserRepository
import ru.aqrc.project.api.model.repository.UserRepository
import ru.aqrc.project.api.service.AccountService
import ru.aqrc.project.api.service.IAccountService
import ru.aqrc.project.api.service.IUserService
import ru.aqrc.project.api.service.UserService
import ru.aqrc.project.api.web.Router
import ru.aqrc.project.api.web.controller.AccountController
import ru.aqrc.project.api.web.controller.IAccountController
import ru.aqrc.project.api.web.controller.IUserController
import ru.aqrc.project.api.web.controller.UserController
import javax.sql.DataSource


object ModulesConfig {
    private val router = module {
        single { Router(get(), get()) }
    }

    private val dataModule = module {
        single<DataSource> {
            HikariConfig()
                .apply { this.jdbcUrl = getProperty("db.jdbc.url") }
                .apply { this.username = getProperty("db.username") }
                .apply { this.password = getProperty("db.password") }
                .apply { this.maximumPoolSize = 6 }
                .let(::HikariDataSource)
        }
        single<IDatabaseInitializer> { DatabaseInitializer(get()) }
        single<ITransactor> { Transactor(get()) }
    }

    private val accountModule = module {
        single<IAccountController> { AccountController(get()) }
        single<IAccountService> { AccountService(get()) }
        single<IAccountRepository> { AccountRepository(get()) }
    }

    private val userModule = module {
        single<IUserController> { UserController(get()) }
        single<IUserService> { UserService(get(), get()) }
        single<IUserRepository> { UserRepository(get()) }
    }

    val apiModules = listOf(
        dataModule,
        router,
        accountModule,
        userModule
    )
}
