package ru.aqrc.project.api.service

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import ru.aqrc.project.api.model.Account
import ru.aqrc.project.api.model.repository.IAccountRepository
import ru.aqrc.project.api.service.exception.EntityNotFoundException
import java.util.*
import java.util.concurrent.CompletableFuture

interface IAccountService {
    fun findById(accountId: UUID): CompletableFuture<Account>
}

class AccountService(
    private val accountRepository: IAccountRepository
) : IAccountService {
    override fun findById(accountId: UUID): CompletableFuture<Account> = GlobalScope.future {
        accountRepository.findByIdAsync(accountId).await() ?: throwNotFound(accountId)
    }

    private fun throwNotFound(accountId: UUID): Nothing = throw EntityNotFoundException("Account $accountId not found.")
}
