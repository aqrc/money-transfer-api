package ru.aqrc.project.api.web.controller

import io.javalin.http.Context
import ru.aqrc.project.api.model.User
import java.util.*

interface IUserController {
    fun create(ctx: Context)
    fun get(ctx: Context)
}

class UserController : IUserController {
    override fun create(ctx: Context) {
        ctx.bodyValidator<User>()
            .check({ !it.name.isBlank() })
            .get()
            .let { ctx.json(User(UUID.randomUUID(), it.name)) }
    }

    override fun get(ctx: Context) {
        ctx.pathParam("id", UUID::class.java)
            .get()
            .let { ctx.json(User(it, "user name")) }
    }
}