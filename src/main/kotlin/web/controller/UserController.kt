package ru.aqrc.project.api.web.controller

import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import ru.aqrc.project.api.model.User
import ru.aqrc.project.api.service.IUserService
import ru.aqrc.project.api.web.controller.extensions.asDTO
import ru.aqrc.project.api.web.controller.extensions.asModel
import ru.aqrc.project.api.web.dto.UserDTO
import java.util.*
import java.util.concurrent.CompletableFuture

interface IUserController {
    fun create(ctx: Context)
    fun get(ctx: Context)
    fun createAccount(ctx: Context)
    fun getAccounts(ctx: Context)
}

class UserController(
    private val userService: IUserService
) : IUserController {
    override fun create(ctx: Context) {
        ctx.bodyValidator<UserDTO>()
            .check({ !it.name.isBlank() })
            .get().asModel()
            .let(userService::create)
            .thenApply { createdUser -> ctx.json(createdUser.asDTO()) }
            .let(ctx::result)
    }

    override fun get(ctx: Context) {
        ctx.pathParam("id", UUID::class.java)
            .get()
            .let(userService::findById)
            .thenApply { user -> ctx.json(user.asDTO()) }
            .let(ctx::result)
    }

    override fun createAccount(ctx: Context) {
        ctx.pathParam("id", UUID::class.java)
            .get()
            .let(userService::createAccount)
            .thenApply { ctx.json(it.asDTO()) }
            .let(ctx::result)
    }

    override fun getAccounts(ctx: Context) {
        ctx.pathParam("id", UUID::class.java)
            .get()
            .let(userService::getAccounts)
            .thenApply { accounts ->
                accounts
                    .map { it.asDTO() }
                    .let { mapOf("accounts" to it) }
                    .let { ctx.json(it) }
            }
            .let(ctx::result)
    }
}