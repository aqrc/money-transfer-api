package ru.aqrc.project.api.web

import io.javalin.apibuilder.ApiBuilder.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.aqrc.project.api.web.controller.IAccountController
import ru.aqrc.project.api.web.controller.IUserController

class Router(
    private val accountController: IAccountController,
    private val userController: IUserController
) {
    private companion object {
       val logger: Logger = LoggerFactory.getLogger(Router::class.java)
    }

    fun endpoints(): () -> Unit = {
        before { ctx -> logger.info(ctx.req.method + " " + ctx.req.requestURL.toString())}

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
