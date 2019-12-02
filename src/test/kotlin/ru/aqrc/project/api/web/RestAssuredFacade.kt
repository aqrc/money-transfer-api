package ru.aqrc.project.api.web

import com.fasterxml.jackson.databind.type.TypeFactory
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.response.ValidatableResponse
import ru.aqrc.project.api.web.dto.AccountDTO
import ru.aqrc.project.api.web.dto.MoneyDTO
import ru.aqrc.project.api.web.dto.UserDTO
import java.lang.reflect.Type
import java.util.*

object RestAssuredFacade {

    fun getUser(
        userId: String,
        expectedStatusCode: Int = 200,
        assert: ValidatableResponse.() -> Unit = {}
    ): UserDTO? {
        return get("/users/$userId", expectedStatusCode, assert, UserDTO::class.java)
    }

    fun postUser(
        userBody: UserDTO,
        expectedStatusCode: Int = 200,
        assert: ValidatableResponse.() -> Unit = {}
    ): UserDTO? {
        return post("/users", userBody, expectedStatusCode, assert, UserDTO::class.java)
 }

    fun postUserAccount(
        userId: String,
        expectedStatusCode: Int = 200,
        assert: ValidatableResponse.() -> Unit = {}
    ): AccountDTO? {
        return post("/users/$userId/account", null, expectedStatusCode, assert, AccountDTO::class.java)
    }

    fun getUserAccounts(
        userId: String,
        expectedStatusCode: Int = 200,
        assert: ValidatableResponse.() -> Unit = {}
    ): Map<String, List<AccountDTO>?>? {
        val typeFactory = TypeFactory.defaultInstance()
        val mapOfStringToListOfAccountsTypeMRef = typeFactory.constructMapType(
            Map::class.java,
            typeFactory.constructType(String::class.java),
            typeFactory.constructCollectionType(List::class.java, AccountDTO::class.java)
        )
        return get("/users/$userId/accounts", expectedStatusCode, assert, mapOfStringToListOfAccountsTypeMRef)
    }

    fun getAccount(
        accountId: String,
        expectedStatusCode: Int = 200,
        assert: ValidatableResponse.() -> Unit = {}
    ): AccountDTO? {
        return get("/accounts/$accountId", expectedStatusCode, assert, AccountDTO::class.java)
    }

    fun postAccountDeposit(
        accountId: String,
        moneyDTO: MoneyDTO,
        expectedStatusCode: Int = 200,
        assert: ValidatableResponse.() -> Unit = {}
    ): AccountDTO? {
        return post("/accounts/$accountId/deposit", moneyDTO, expectedStatusCode, assert, AccountDTO::class.java)
    }

    fun postAccountWithdrawal(
        accountId: String,
        moneyDTO: MoneyDTO,
        expectedStatusCode: Int = 200,
        assert: ValidatableResponse.() -> Unit = {}
    ): AccountDTO? {
        return post("/accounts/$accountId/withdrawal", moneyDTO, expectedStatusCode, assert, AccountDTO::class.java)
    }

    fun postTransfer(
        fromAccountId: String,
        toAccountId: String,
        moneyDTO: MoneyDTO,
        expectedStatusCode: Int = 200,
        assert: ValidatableResponse.() -> Unit = {}
    ): Unit? {
        return post("/accounts/$fromAccountId/transfer/$toAccountId", moneyDTO, expectedStatusCode, assert, null)
    }

    private fun <T> get(
        path: String,
        expectedStatusCode: Int,
        assert: ValidatableResponse.() -> Unit = {},
        extractAs: Type
    ): T? =
        When {
            get(path)
        } Then {
            statusCode(expectedStatusCode)
            assert()
        } Extract {
            if (expectedStatusCode == 200)
                `as`(extractAs)
            else null
        }

    private fun <T, S> post(
        path: String,
        body: S?,
        expectedStatusCode: Int,
        assert: ValidatableResponse.() -> Unit = {},
        extractAs: Class<T>?
    ): T? =
        Given {
            body(body ?: "")
        } When {
            post(path)
        } Then {
            statusCode(expectedStatusCode)
            assert()
        } Extract {
            if (expectedStatusCode == 200 && extractAs != null)
                `as`(extractAs)
            else null
        }

}
