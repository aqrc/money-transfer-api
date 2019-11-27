package ru.aqrc.project.api.model

import java.math.BigDecimal
import java.util.*

data class Account(
    val id: UUID,
    val userId: UUID,
    val amount: BigDecimal
)