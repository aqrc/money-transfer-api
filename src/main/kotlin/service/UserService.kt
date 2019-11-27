package ru.aqrc.project.api.service

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import ru.aqrc.project.api.model.Account
import ru.aqrc.project.api.model.User
import ru.aqrc.project.api.model.repository.IAccountRepository
import ru.aqrc.project.api.model.repository.IUserRepository
import java.util.*
import java.util.concurrent.CompletableFuture

interface IUserService {
    fun create(user: User): CompletableFuture<User>
    fun findById(userId: UUID): CompletableFuture<User?>
    fun createAccount(userId: UUID): CompletableFuture<Account>
}

class UserService(
    private val accountRepository: IAccountRepository,
    private val userRepository: IUserRepository
): IUserService {
    override fun create(user: User): CompletableFuture<User> = GlobalScope.future {
        userRepository.createAsync(user).await()
    }

    override fun findById(userId: UUID): CompletableFuture<User?> = GlobalScope.future {
        userRepository.findByIdAsync(userId).await()
    }

    override fun createAccount(userId: UUID): CompletableFuture<Account> = GlobalScope.future {
        accountRepository.createAccountAsync(userId).await()
    }
}

