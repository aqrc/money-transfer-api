package ru.aqrc.project.api.web

import io.javalin.apibuilder.ApiBuilder.*
import ru.aqrc.project.api.web.controller.IUserController

class Router(
    private val userController: IUserController
) {
    fun endpoints(): () -> Unit = {
        path("users") {
            get(":id", userController::get)
            post(userController::create)
        }
    }
}
