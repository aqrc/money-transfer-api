package ru.aqrc.project.api.web

import io.javalin.Javalin
import org.eclipse.jetty.http.HttpStatus
import org.slf4j.LoggerFactory
import ru.aqrc.project.api.service.exception.EntityNotFoundException
import ru.aqrc.project.api.web.dto.ErrorResponse


object ExceptionMapper {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun configure(app: Javalin) {
        app.exception(EntityNotFoundException::class.java) { exception, ctx ->
            logger.warn("Entity not found", exception)
            ctx.json(ErrorResponse(exception.message))
                .status(HttpStatus.NOT_FOUND_404)
        }

        app.exception(Exception::class.java) { exception, ctx ->
            logger.error("Unknown error", exception)
            ctx.json(ErrorResponse("Internal server error"))
                .status(HttpStatus.INTERNAL_SERVER_ERROR_500)
        }
    }
}