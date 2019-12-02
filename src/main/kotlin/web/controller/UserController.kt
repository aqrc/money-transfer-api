package ru.aqrc.project.api.web.controller

import io.javalin.http.Context
import ru.aqrc.project.api.model.Account
import ru.aqrc.project.api.model.User
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
            .thenApply(User::asDTO)
            .let(ctx::json)
    }

    override fun getUser(ctx: Context) {
        ctx.getValidatedUserId()
            .let(userService::findById)
            .thenApply(User::asDTO)
            .let(ctx::json)
    }

    override fun createAccount(ctx: Context) {
        ctx.getValidatedUserId()
            .let(userService::createAccount)
            .thenApply(Account::asDTO)
            .let(ctx::json)
    }

    override fun getAccounts(ctx: Context) {
        ctx.getValidatedUserId()
            .let(userService::getAccounts)
            .thenApply { accounts ->
                accounts
                    .map(Account::asDTO)
                    .let { mapOf("accounts" to it) }
            }
            .let(ctx::json)
    }

    private fun Context.getValidatedUserId(): UUID = this.pathParam("id", UUID::class.java).get()

    private fun Context.getValidatedUserDTO(): UserDTO {
        return this.bodyValidator<UserDTO>()
            .check({ !it.name.isBlank() })
            .get()
    }
}