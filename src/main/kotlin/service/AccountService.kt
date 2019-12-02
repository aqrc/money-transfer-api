package ru.aqrc.project.api.service

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import ru.aqrc.project.api.model.Account
import ru.aqrc.project.api.model.repository.IAccountRepository
import ru.aqrc.project.api.web.dto.MoneyDTO
import java.util.*
import java.util.concurrent.CompletableFuture

interface IAccountService {
    fun findById(accountId: UUID): CompletableFuture<Account>
    fun deposit(accountId: UUID, moneyDTO: MoneyDTO): CompletableFuture<Account>
    fun withdrawal(accountId: UUID, moneyDTO: MoneyDTO): CompletableFuture<Account>
    fun transfer(fromAccountId: UUID, toAccountId: UUID, moneyDTO: MoneyDTO): CompletableFuture<Unit>
}

class AccountService(
    private val accountRepository: IAccountRepository
) : IAccountService {
    override fun findById(accountId: UUID): CompletableFuture<Account> = GlobalScope.future {
        accountRepository.findByIdAsync(accountId).await()
    }

    override fun deposit(accountId: UUID, moneyDTO: MoneyDTO): CompletableFuture<Account> = GlobalScope.future {
        accountRepository.increaseAmountAsync(accountId, moneyDTO.amount).await()
    }

    override fun withdrawal(accountId: UUID, moneyDTO: MoneyDTO): CompletableFuture<Account> = GlobalScope.future {
        accountRepository.decreaseAmountAsync(accountId, moneyDTO.amount).await()
    }

    override fun transfer(fromAccountId: UUID, toAccountId: UUID, moneyDTO: MoneyDTO) = GlobalScope.future {
        accountRepository.transferAsync(fromAccountId, toAccountId, moneyDTO.amount).await()
    }
}
