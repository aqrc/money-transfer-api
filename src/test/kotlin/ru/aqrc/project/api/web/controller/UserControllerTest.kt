package ru.aqrc.project.api.web.controller

import io.javalin.Javalin
import io.restassured.RestAssured
import io.restassured.config.JsonConfig.jsonConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.path.json.config.JsonPathConfig
import org.hamcrest.CoreMatchers.*
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.core.Koin
import ru.aqrc.project.api.config.AppConfig
import ru.aqrc.project.api.config.KoinConfig
import ru.aqrc.project.api.web.RestAssuredFacade.getUser
import ru.aqrc.project.api.web.RestAssuredFacade.getUserAccounts
import ru.aqrc.project.api.web.RestAssuredFacade.postUser
import ru.aqrc.project.api.web.RestAssuredFacade.postUserAccount
import ru.aqrc.project.api.web.dto.AccountDTO
import ru.aqrc.project.api.web.dto.UserDTO
import java.math.BigDecimal
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {
    private lateinit var api: Javalin
    private lateinit var koin: Koin

    private companion object {
        const val ID_PATH = "id"
        const val USERNAME_PATH = "name"
        const val USERNAME = "John Doe"
        const val USER_ID_PATH = "userId"
        const val AMOUNT_PATH = "amount"
        const val ACCOUNTS_PATH = "accounts"
    }

    @BeforeAll
    fun startApi() {
        koin = KoinConfig.init()
        api = AppConfig.startApplication()

        RestAssured.port = api.port()
        RestAssured.config = RestAssuredConfig
            .config()
            .jsonConfig(
                jsonConfig()
                    .numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL)
            )
    }

    @AfterAll
    fun stopApi() {
        api.stop()
        koin.close()
    }

    @Test
    fun `should create user and return its id`() {
        postUser(UserDTO(name = USERNAME)) {
            body(ID_PATH, notNullValue())
            body(USERNAME_PATH, equalTo(USERNAME))
        }
    }

    @Test
    fun `should get user after creation by its id`() {
        val user = postUser(UserDTO(name = USERNAME))

        getUser(user.id.toString()) {
            body(ID_PATH, equalTo(user.id.toString()))
            body(USERNAME_PATH, equalTo(USERNAME))
        }
    }

    @Test
    fun `should respond with 404 and message on get user when user not found`() {
        val unknownUserId = UUID.randomUUID().toString()
        getUser(unknownUserId, 404) {
            body("error.message", containsStringIgnoringCase(unknownUserId))
        }
    }

    @Test
    fun `should create account for user with zeroed amount and return it`() {
        val user = postUser(UserDTO(name = USERNAME))

        postUserAccount(user.id.toString()) {
            body(ID_PATH, notNullValue())
            body(USER_ID_PATH, equalTo(user.id.toString()))
            body(AMOUNT_PATH, equalTo(BigDecimal.valueOf(0, 8)))
        }
    }

    @Test
    fun `should get empty list of user accounts when they were not created`() {
        val user = postUser(UserDTO(name = USERNAME))

        getUserAccounts(user.id.toString()) {
            body(ACCOUNTS_PATH, equalTo(emptyList<AccountDTO>()))
        }
    }

    @Test
    fun `should get list of user accounts when they were created`() {
        val user = postUser(UserDTO(name = USERNAME))

        val account1 = postUserAccount(user.id.toString())
        val account2 = postUserAccount(user.id.toString())

        getUserAccounts(user.id.toString()) {
            body("$ACCOUNTS_PATH.$ID_PATH", hasItems(account1.id.toString(), account2.id.toString()))
            body("$ACCOUNTS_PATH.$ID_PATH", hasSize<List<*>>(2))
        }
    }

    @Test
    fun `should respond with 404 and message on get user accounts when user not found`() {
        val unknownUserId = UUID.randomUUID().toString()
        getUserAccounts(unknownUserId, 404) {
            body("error.message", containsStringIgnoringCase(unknownUserId))
        }
    }
}