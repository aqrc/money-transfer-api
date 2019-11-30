package ru.aqrc.project.api.web.dto

class ErrorResponse private constructor(
    val error: ErrorDetails
) {
    constructor(message: String?) : this(ErrorDetails(message))
}

class ErrorDetails(
    val message: String?
)