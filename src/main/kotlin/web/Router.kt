package ru.aqrc.project.api.web

import io.javalin.apibuilder.ApiBuilder.*
import ru.aqrc.project.api.web.controller.IAccountController
import ru.aqrc.project.api.web.controller.IUserController

class Router(
    private val accountController: IAccountController,
    private val userController: IUserController
) {
    fun endpoints(): () -> Unit = {
        path("users") {
            get(":id", userController::get)
            get(":id/accounts", userController::getAccounts)
            post(userController::create)
            post(":id/account", userController::createAccount)
        }
        path("accounts") {
            get(":id", accountController::get)
        }
    }
}
