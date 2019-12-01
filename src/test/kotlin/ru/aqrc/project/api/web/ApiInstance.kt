package ru.aqrc.project.api.web

import io.javalin.Javalin
import io.restassured.RestAssured
import io.restassured.config.JsonConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.path.json.config.JsonPathConfig
import org.koin.core.Koin
import ru.aqrc.project.api.config.AppConfig
import ru.aqrc.project.api.config.KoinConfig

object ApiInstance {
    private lateinit var api: Javalin
    private lateinit var koin: Koin

    fun startApi() {
        koin = KoinConfig.init()
        api = AppConfig.startApplication()

        RestAssured.port = api.port()
        RestAssured.config = RestAssuredConfig
            .config()
            .jsonConfig(
                JsonConfig.jsonConfig()
                    .numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL)
            )
    }

    fun stopApi() {
        api.stop()
        koin.close()
    }
}