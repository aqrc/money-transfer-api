package ru.aqrc.project.api.web

import io.javalin.apibuilder.ApiBuilder.get

object Router {

    fun endpoints(): () -> Unit = {
        get("/") { ctx -> ctx.result("api is here") }
    }
}
