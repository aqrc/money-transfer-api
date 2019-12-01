package ru.aqrc.project.api.web.controller

import io.javalin.http.Context
import ru.aqrc.project.api.service.IUserService
import ru.aqrc.project.api.web.controller.extensions.asDTO
import ru.aqrc.project.api.web.controller.extensions.asModel
import ru.aqrc.project.api.web.dto.UserDTO
import java.util.*

interface IUserController {
    fun createUser(ctx: Context)
    fun getUser(ctx: Context)
    fun createAccount(ctx: Context)
    fun getAccounts(ctx: Context)
}

class UserController(
    private val userService: IUserService
) : IUserController {
    override fun createUser(ctx: Context) {
        ctx.getValidatedUserDTO().asModel()
            .let(userService::create)
            .thenApply { createdUser -> ctx.json(createdUser.asDTO()) }
            .let(ctx::result)
    }

    override fun getUser(ctx: Context) {
        ctx.getValidatedUserId()
            .let(userService::findById)
            .thenApply { user -> ctx.json(user.asDTO()) }
            .let(ctx::result)
    }

    override fun createAccount(ctx: Context) {
        ctx.getValidatedUserId()
            .let(userService::createAccount)
            .thenApply { ctx.json(it.asDTO()) }
            .let(ctx::result)
    }

    override fun getAccounts(ctx: Context) {
        ctx.getValidatedUserId()
            .let(userService::getAccounts)
            .thenApply { accounts ->
                accounts
                    .map { it.asDTO() }
                    .let { mapOf("accounts" to it) }
                    .let { ctx.json(it) }
            }
            .let(ctx::result)
    }

    private fun Context.getValidatedUserId(): UUID = this.pathParam("id", UUID::class.java).get()

    private fun Context.getValidatedUserDTO(): UserDTO {
        return this.bodyValidator<UserDTO>()
            .check({ !it.name.isBlank() })
            .get()
    }
}