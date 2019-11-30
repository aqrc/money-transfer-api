package ru.aqrc.project.api.web.controller

import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import ru.aqrc.project.api.service.IAccountService
import ru.aqrc.project.api.web.controller.extensions.asDTO
import java.util.*

interface IAccountController {
    fun get(ctx: Context)
}

class AccountController(
    private val accountService: IAccountService
) : IAccountController {

    override fun get(ctx: Context) {
        ctx.pathParam("id", UUID::class.java)
            .get()
            .let(accountService::findById)
            .thenApply { account ->
                account ?: throw NotFoundResponse("Account not found")
                ctx.json(account.asDTO())
            }
            .let(ctx::result)
    }
}