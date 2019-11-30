package ru.aqrc.project.api.web;

import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.response.ValidatableResponse
import ru.aqrc.project.api.web.dto.UserDTO

object RestAssuredFacade {

    fun getUser(userId: String, expectedStatusCode: Int, assert: ValidatableResponse.() -> Unit = {}): UserDTO {
        return get("/users/$userId", expectedStatusCode, assert) Extract { `as`(UserDTO::class.java) }
    }

    fun postUser(
        userBody: UserDTO,
        expectedStatusCode: Int,
        assertion: ValidatableResponse.() -> Unit = {}
    ): UserDTO {
        return post("/users", userBody, expectedStatusCode, assertion) Extract { `as`(UserDTO::class.java) }
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
        assertion: ValidatableResponse.() -> Unit = {}
    ): ValidatableResponse =
        Given {
            body(body)
        } When {
            post(path)
        } Then {
            statusCode(expectedStatusCode)
            assertion()
        }
    
}
