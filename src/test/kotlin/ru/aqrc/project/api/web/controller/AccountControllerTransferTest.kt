package ru.aqrc.project.api.web.controller

import kotlinx.coroutines.*
import org.hamcrest.CoreMatchers.containsStringIgnoringCase
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.*
import ru.aqrc.project.api.web.ApiInstance
import ru.aqrc.project.api.web.RestAssuredFacade.getAccount
import ru.aqrc.project.api.web.RestAssuredFacade.postAccountDeposit
import ru.aqrc.project.api.web.RestAssuredFacade.postTransfer
import ru.aqrc.project.api.web.RestAssuredFacade.postUser
import ru.aqrc.project.api.web.RestAssuredFacade.postUserAccount
import ru.aqrc.project.api.web.dto.AccountDTO
import ru.aqrc.project.api.web.dto.MoneyDTO
import ru.aqrc.project.api.web.dto.UserDTO
import java.math.BigDecimal
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountControllerTransferTest {
    private lateinit var fromUser: UserDTO
    private lateinit var fromAccount: AccountDTO
    private lateinit var toUser: UserDTO
    private lateinit var toAccount: AccountDTO

    private companion object {
        const val ID_PATH = "id"
        const val USER_ID_PATH = "userId"
        const val AMOUNT_PATH = "amount"
        const val ERROR_MESSAGE_PATH = "error.message"
    }

    @BeforeAll
    fun startApi() {
        ApiInstance.startApi()
    }

    @AfterAll
    fun stopApi() {
        ApiInstance.stopApi()
    }

    @BeforeEach
    fun setUp() {
        fromUser = postUser(UserDTO(name = "User From"))!!
        fromAccount = postUserAccount(fromUser.id.toString())!!

        toUser = postUser(UserDTO(name = "User To"))!!
        toAccount = postUserAccount(toUser.id.toString())!!
    }

    @Test
    fun `should respond with 404 and message when sender's account not found`() {
        val unknownAccountId = UUID.randomUUID().toString()
        val money = MoneyDTO(BigDecimal.valueOf(100))

        postTransfer(unknownAccountId, toAccount.id.toString(), money, 404) {
            body(ERROR_MESSAGE_PATH, containsStringIgnoringCase(unknownAccountId))
        }
    }

    @Test
    fun `should respond with 404 and message when receiver's account not found`() {
        val unknownAccountId = UUID.randomUUID().toString()
        val money = MoneyDTO(BigDecimal.valueOf(100))

        postTransfer(fromAccount.id.toString(), unknownAccountId, money, 404) {
            body(ERROR_MESSAGE_PATH, containsStringIgnoringCase(unknownAccountId))
        }
    }

    @Test
    fun `should respond with 400 and message on transfer when body has not positive amount`() {
        val assert400WithError = { money: MoneyDTO ->
            postTransfer(fromAccount.id.toString(), toAccount.id.toString(), money, 400) {
                body(ERROR_MESSAGE_PATH, containsStringIgnoringCase("positive"))
            }
        }

        val zeroTransfer = MoneyDTO(amount = BigDecimal.valueOf(0, 8))
        assert400WithError(zeroTransfer)

        val negativeTransfer = MoneyDTO(amount = BigDecimal.valueOf(-10000, 8))
        assert400WithError(negativeTransfer)
    }

    @Test
    fun `should respond with 400 and message on transfer when sender doesn't have enough money on account`() {
        val money = MoneyDTO(amount = BigDecimal.valueOf(1000, 8))
        postTransfer(fromAccount.id.toString(), toAccount.id.toString(), money, 400) {
            body(ERROR_MESSAGE_PATH, containsStringIgnoringCase(fromAccount.id.toString()))
        }
    }

    @Test
    fun `should move money from sender's account to receiver's account`() {
        val deposit = MoneyDTO(amount = BigDecimal.valueOf(1000, 8))
        postAccountDeposit(fromAccount.id.toString(), deposit) {
            body(AMOUNT_PATH, equalTo(deposit.amount))
        }

        postTransfer(fromAccount.id.toString(), toAccount.id.toString(), deposit)

        getAccount(fromAccount.id.toString()) {
            body(ID_PATH, equalTo(fromAccount.id.toString()))
            body(USER_ID_PATH, equalTo(fromUser.id.toString()))
            body(AMOUNT_PATH, equalTo(BigDecimal.valueOf(0, 8)))
        }

        getAccount(toAccount.id.toString()) {
            body(ID_PATH, equalTo(toAccount.id.toString()))
            body(USER_ID_PATH, equalTo(toUser.id.toString()))
            body(AMOUNT_PATH, equalTo(deposit.amount))
        }
    }
}