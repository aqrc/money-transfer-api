package ru.aqrc.project.api.web.controller

import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import ru.aqrc.project.api.model.User
import ru.aqrc.project.api.service.IUserService
import java.util.*

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
        ctx.bodyValidator<User>()
            .check({ !it.name.isBlank() })
            .get()
            .let(userService::create)
            .thenApply { createdUser -> ctx.json(createdUser) }
            .let(ctx::result)
    }

    override fun get(ctx: Context) {
        ctx.pathParam("id", UUID::class.java)
            .get()
            .let(userService::findById)
            .thenApply { user ->
                user ?: throw NotFoundResponse("User not found")
                ctx.json(user)
            }
            .let(ctx::result)
    }

    override fun createAccount(ctx: Context) {
        ctx.pathParam("id", UUID::class.java)
            .get()
            .let(userService::createAccount)
            .thenApply { ctx.json(it) }
            .let(ctx::result)
    }

    override fun getAccounts(ctx: Context) {
        ctx.pathParam("id", UUID::class.java)
            .get()
            .let(userService::getAccounts)
            .thenApply { ctx.json(mapOf("accounts" to it)) }
            .let(ctx::result)
    }
}