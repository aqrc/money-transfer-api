package ru.aqrc.project.api.web.controller

import io.javalin.http.Context
import ru.aqrc.project.api.service.IAccountService
import ru.aqrc.project.api.web.controller.extensions.asDTO
import java.util.*

interface IAccountController {
    fun getAccount(ctx: Context)
}

class AccountController(
    private val accountService: IAccountService
) : IAccountController {
    override fun getAccount(ctx: Context) {
        ctx.getValidatedAccountId()
            .let(accountService::findById)
            .thenApply { account -> ctx.json(account.asDTO()) }
            .let(ctx::result)
    }


    private fun Context.getValidatedAccountId(): UUID = this.pathParam("id", UUID::class.java).get()
}