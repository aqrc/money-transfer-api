package ru.aqrc.project.api.web;

import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.path.json.JsonPath
import io.restassured.response.ValidatableResponse
import ru.aqrc.project.api.web.dto.AccountDTO
import ru.aqrc.project.api.web.dto.UserDTO

object RestAssuredFacade {

    fun getUser(
        userId: String,
        expectedStatusCode: Int = 200,
        assert: ValidatableResponse.() -> Unit = {}
    ): UserDTO? {
        return get("/users/$userId", expectedStatusCode, assert)
            .let {
                if (expectedStatusCode == 200)
                    it.Extract { `as`(UserDTO::class.java) }
                else null
            }
    }

    fun postUser(
        userBody: UserDTO,
        expectedStatusCode: Int = 200,
        assert: ValidatableResponse.() -> Unit = {}
    ): UserDTO {
        return post("/users", userBody, expectedStatusCode, assert) Extract { `as`(UserDTO::class.java) }
    }

    fun postUserAccount(
        userId: String,
        expectedStatusCode: Int = 200,
        assert: ValidatableResponse.() -> Unit = {}
    ): AccountDTO {
        return post("/users/$userId/account", "", expectedStatusCode, assert) Extract { `as`(AccountDTO::class.java) }
    }

    fun getUserAccounts(
        userId: String,
        expectedStatusCode: Int = 200,
        assert: ValidatableResponse.() -> Unit = {}
    ): List<AccountDTO>? {
        return get("/users/$userId/accounts", expectedStatusCode, assert)
            .let {
                if (expectedStatusCode == 200)
                    it.Extract { JsonPath.from(response().asInputStream()).getList<AccountDTO>("accounts") }
                else null
            }
    }

    private fun get(
        path: String,
        expectedStatusCode: Int,
        assert: ValidatableResponse.() -> Unit = {}
    ): ValidatableResponse =
        When {
            get(path)
        } Then {
            statusCode(expectedStatusCode)
            this.assert()
        }

    private fun <T> post(
        path: String,
        body: T,
        expectedStatusCode: Int,
        assert: ValidatableResponse.() -> Unit = {}
    ): ValidatableResponse =
        Given {
            body(body)
        } When {
            post(path)
        } Then {
            statusCode(expectedStatusCode)
            assert()
        }

}
