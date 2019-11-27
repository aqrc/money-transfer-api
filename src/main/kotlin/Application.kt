package ru.aqrc.project.api

import io.javalin.Javalin

fun main(args: Array<String>) {
    Javalin
        .create()
        .get("/") { ctx -> ctx.result("api is here") }
        .start(7000)
}