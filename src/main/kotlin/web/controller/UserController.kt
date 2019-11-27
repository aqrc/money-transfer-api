package ru.aqrc.project.api.web.controller

import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import ru.aqrc.project.api.model.User
import ru.aqrc.project.api.service.IUserService
import java.util.*

interface IUserController {
    fun create(ctx: Context)
    fun get(ctx: Context)
}

class UserController(
    private val userService: IUserService
) : IUserController {
    override fun create(ctx: Context) {
        ctx.bodyValidator<User>()
            .check({ !it.name.isBlank() })
            .get()
            .let(userService::create)
            .let { createdUser -> ctx.json(createdUser) }
    }

    override fun get(ctx: Context) {
        ctx.pathParam("id", UUID::class.java)
            .get()
            .let(userService::findById)
            ?.let { ctx.json(it) }
            ?: throw NotFoundResponse("User not found")
    }
}