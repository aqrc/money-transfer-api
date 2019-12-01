package ru.aqrc.project.api.service.exception

class EntityNotFoundException(message: String): RuntimeException(message)

class NotEnoughMoneyOnBalance(message: String): RuntimeException(message)