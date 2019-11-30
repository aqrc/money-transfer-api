package ru.aqrc.project.api.web.dto

import java.math.BigDecimal
import java.util.*

class AccountDTO(
    val id: UUID,
    val userId: UUID,
    val amount: BigDecimal
)