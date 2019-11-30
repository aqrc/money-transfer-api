package ru.aqrc.project.api.service

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import ru.aqrc.project.api.model.Account
import ru.aqrc.project.api.model.User
import ru.aqrc.project.api.model.repository.IAccountRepository
import ru.aqrc.project.api.model.repository.IUserRepository
import ru.aqrc.project.api.service.exception.EntityNotFoundException
import java.util.*
import java.util.concurrent.CompletableFuture

interface IUserService {
    fun create(user: User): CompletableFuture<User>
    fun findById(userId: UUID): CompletableFuture<User>
    fun createAccount(userId: UUID): CompletableFuture<Account>
    fun getAccounts(userId: UUID): CompletableFuture<List<Account>>
}

class UserService(
    private val accountRepository: IAccountRepository,
    private val userRepository: IUserRepository
): IUserService {
    override fun create(user: User): CompletableFuture<User> = GlobalScope.future {
        userRepository.createAsync(user).await()
    }

    override fun findById(userId: UUID): CompletableFuture<User> = GlobalScope.future {
        userRepository.findByIdAsync(userId).await() ?: throwNotFound(userId)
    }

    override fun createAccount(userId: UUID): CompletableFuture<Account> = GlobalScope.future {
        userRepository.findByIdAsync(userId).await() ?: throwNotFound(userId)
        accountRepository.createAccountAsync(userId).await()
    }

    override fun getAccounts(userId: UUID): CompletableFuture<List<Account>> = GlobalScope.future {
        userRepository.findByIdAsync(userId).await() ?: throwNotFound(userId)
        accountRepository.findByUserIdAsync(userId).await()
    }

    private fun throwNotFound(userId: UUID): Nothing = throw EntityNotFoundException("User $userId not found.")
}

