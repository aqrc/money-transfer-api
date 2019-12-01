package ru.aqrc.project.api.web.controller

import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ru.aqrc.project.api.web.ApiInstance
import ru.aqrc.project.api.web.RestAssuredFacade.getAccount
import ru.aqrc.project.api.web.RestAssuredFacade.postUser
import ru.aqrc.project.api.web.RestAssuredFacade.postUserAccount
import ru.aqrc.project.api.web.dto.UserDTO
import java.math.BigDecimal

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountControllerTest {

    private companion object {
        const val ID_PATH = "id"
        const val USER_ID_PATH = "userId"
        const val AMOUNT_PATH = "amount"
    }

    @BeforeAll
    fun startApi() {
        ApiInstance.startApi()
    }

    @AfterAll
    fun stopApi() {
        ApiInstance.stopApi()
    }

    @Test
    fun `should get account after creation by its id`() {
        val user = postUser(UserDTO(name = "random name"))!!
        val account = postUserAccount(user.id.toString())!!

        getAccount(account.id.toString()) {
            body(ID_PATH, equalTo(account.id.toString()))
            body(USER_ID_PATH, equalTo(user.id.toString()))
            body(AMOUNT_PATH, equalTo(BigDecimal.valueOf(0, 8)))
        }
    }

}