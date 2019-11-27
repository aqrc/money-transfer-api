package ru.aqrc.project.api.service

import ru.aqrc.project.api.model.User
import ru.aqrc.project.api.model.repository.IUserRepository
import java.util.*

interface IUserService {
    fun create(user: User): User
    fun findById(userId: UUID): User?
}

class UserService(
    private val userRepository: IUserRepository
): IUserService {
    override fun create(user: User): User {
        return userRepository.create(user)
            .let { user.copy(id = it) }
    }

    override fun findById(userId: UUID): User? {
        return userRepository.findById(userId)
    }
}

