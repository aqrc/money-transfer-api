package ru.aqrc.project.api.config

import org.koin.dsl.module
import ru.aqrc.project.api.web.Router
import ru.aqrc.project.api.web.controller.IUserController
import ru.aqrc.project.api.web.controller.UserController


object ModulesConfig {
    private val router = module {
        single { Router(get()) }
    }

    private val userModule = module {
        single<IUserController> { UserController() }
    }

    val apiModules = listOf(
        router,
        userModule
    )
}
