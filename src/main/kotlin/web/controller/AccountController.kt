package ru.aqrc.project.api.web.controller

import io.javalin.http.Context
import ru.aqrc.project.api.model.Account
import ru.aqrc.project.api.service.IAccountService
import ru.aqrc.project.api.web.controller.extensions.asDTO
import ru.aqrc.project.api.web.dto.MoneyDTO
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture

interface IAccountController {
    fun getAccount(ctx: Context)
    fun deposit(ctx: Context)
    fun withdrawal(ctx: Context)
    fun transfer(ctx: Context)
}

class AccountController(
    private val accountService: IAccountService
) : IAccountController {
    private companion object {
        const val MAX_SCALE = 8
    }

    override fun getAccount(ctx: Context) {
        ctx.getValidatedAccountId()
            .let(accountService::findById)
            .thenApply { account -> ctx.json(account.asDTO()) }
            .let(ctx::result)
    }

    override fun deposit(ctx: Context) = ctx.handleMoneyOperation(accountService::deposit)

    override fun withdrawal(ctx: Context) = ctx.handleMoneyOperation(accountService::withdrawal)

    private fun Context.handleMoneyOperation(operation: (UUID, MoneyDTO) -> CompletableFuture<Account>) {
        val accountId = this.getValidatedAccountId()
        operation(accountId, this.getValidatedMoneyDTO())
            .thenApply { account -> this.json(account.asDTO()) }
            .let(this::result)
    }

    override fun transfer(ctx: Context) {
        val fromAccountId = ctx.getValidatedAccountId("fromId")
        val toAccountId = ctx.getValidatedAccountId("toId")
        ctx.getValidatedMoneyDTO()
            .let { money -> accountService.transfer(fromAccountId, toAccountId, money) }
            .let(ctx::json)
    }

    private fun Context.getValidatedAccountId(idPath: String = "id"): UUID = this.pathParam(idPath, UUID::class.java).get()

    private fun Context.getValidatedMoneyDTO(): MoneyDTO {
        return this.bodyValidator<MoneyDTO>()
            .check({ it.amount.scale() <= MAX_SCALE }, "The maximum supported scale is $MAX_SCALE")
            .check({ it.amount > BigDecimal.ZERO }, "Amount must be positive")
            .get()
    }
}