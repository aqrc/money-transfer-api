package ru.aqrc.project.api.web.controller

import io.javalin.Javalin
import io.restassured.RestAssured
import org.hamcrest.CoreMatchers.*
import org.junit.jupiter.api.*
import org.koin.core.Koin
import ru.aqrc.project.api.config.AppConfig
import ru.aqrc.project.api.config.KoinConfig
import ru.aqrc.project.api.web.RestAssuredFacade.getUser
import ru.aqrc.project.api.web.RestAssuredFacade.postUser
import ru.aqrc.project.api.web.dto.UserDTO
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {
    private lateinit var api: Javalin
    private lateinit var koin: Koin

    private companion object {
        const val ID_PATH = "id"
        const val USERNAME_PATH = "name"
        const val USERNAME = "John Doe"
    }

    @BeforeAll
    fun startApi() {
        koin = KoinConfig.init()
        api = AppConfig.startApplication()

        RestAssured.port = api.port()
    }

    @AfterAll
    fun stopApi() {
        api.stop()
        koin.close()
    }

    @Test
    fun `should create user and return its id`() {
        postUser(UserDTO(name = USERNAME), 200) {
            body(ID_PATH, notNullValue())
            body(USERNAME_PATH, equalTo(USERNAME))
        }
    }

    @Test
    fun `should get user after creation by its id`() {
        val user = postUser(UserDTO(name = USERNAME), 200)

        getUser(user.id.toString(), 200) {
            body(ID_PATH, equalTo(user.id.toString()))
            body(USERNAME_PATH, equalTo(USERNAME))
        }
    }

    @Test
    fun `should respond with 404 and message when user not found`() {
        val unknownUserId = UUID.randomUUID().toString()
        getUser(unknownUserId, 404) {
            body("error.message", containsStringIgnoringCase(unknownUserId))
        }
    }
}