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
            get(":id", userController::getUser)
            get(":id/accounts", userController::getAccounts)
            post(userController::createUser)
            post(":id/account", userController::createAccount)
        }
        path("accounts") {
            get(":id", accountController::getAccount)
            post(":id/deposit", accountController::deposit)
            post(":id/withdrawal", accountController::withdrawal)
            post(":fromId/transfer/:toId", accountController::transfer)
        }
    }
}
