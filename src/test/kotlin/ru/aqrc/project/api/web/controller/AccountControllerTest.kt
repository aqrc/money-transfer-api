package ru.aqrc.project.api.web.controller

import org.hamcrest.CoreMatchers.*
import org.junit.jupiter.api.*
import ru.aqrc.project.api.web.ApiInstance
import ru.aqrc.project.api.web.RestAssuredFacade.getAccount
import ru.aqrc.project.api.web.RestAssuredFacade.postAccountDeposit
import ru.aqrc.project.api.web.RestAssuredFacade.postAccountWithdrawal
import ru.aqrc.project.api.web.RestAssuredFacade.postUser
import ru.aqrc.project.api.web.RestAssuredFacade.postUserAccount
import ru.aqrc.project.api.web.dto.AccountDTO
import ru.aqrc.project.api.web.dto.MoneyDTO
import ru.aqrc.project.api.web.dto.UserDTO
import java.math.BigDecimal
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountControllerTest {
    private lateinit var user: UserDTO
    private lateinit var account: AccountDTO

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
        user = postUser(UserDTO(name = "random name"))!!
        account = postUserAccount(user.id.toString())!!
    }

    @Test
    fun `should get account after creation by its id`() {
        getAccount(account.id.toString()) {
            body(ID_PATH, equalTo(account.id.toString()))
            body(USER_ID_PATH, equalTo(user.id.toString()))
            body(AMOUNT_PATH, equalTo(BigDecimal.valueOf(0, 8)))
        }
    }

    @Test
    fun `should deposit money on account`() {
        val deposit = MoneyDTO(amount = BigDecimal.valueOf(1_50000000, 8))
        postAccountDeposit(account.id.toString(), deposit) {
            body(ID_PATH, equalTo(account.id.toString()))
            body(USER_ID_PATH, equalTo(user.id.toString()))
            body(AMOUNT_PATH, equalTo(deposit.amount))
        }
    }

    @Test
    fun `should withdrawal money from account`() {
        val deposit = MoneyDTO(amount = BigDecimal.valueOf(1_50000000, 8))
        postAccountDeposit(account.id.toString(), deposit)

        val firstWithdrawal = MoneyDTO(amount = BigDecimal.valueOf(90000000, 8))
        postAccountWithdrawal(account.id.toString(), firstWithdrawal) {
            body(ID_PATH, equalTo(account.id.toString()))
            body(USER_ID_PATH, equalTo(user.id.toString()))
            body(AMOUNT_PATH, equalTo(BigDecimal.valueOf(60000000, 8)))
        }

        val secondWithdrawal = MoneyDTO(amount = BigDecimal.valueOf(60000000, 8))
        postAccountWithdrawal(account.id.toString(), secondWithdrawal ) {
            body(ID_PATH, equalTo(account.id.toString()))
            body(USER_ID_PATH, equalTo(user.id.toString()))
            body(AMOUNT_PATH, equalTo(BigDecimal.valueOf(0, 8)))
        }
    }

    @Test
    fun `should respond with 400 and message on withdrawal when there are not enough money on account`() {
        val withdrawal = MoneyDTO(amount = BigDecimal.valueOf(1, 8))
        postAccountWithdrawal(account.id.toString(), withdrawal, 400) {
            body(ERROR_MESSAGE_PATH, containsStringIgnoringCase(account.id.toString()))
        }
    }

    @Test
    fun `should respond with 400 and message on withdrawal when body has not positive amount`() {
        val assert400WithErrorOnWithdrawal = { money: MoneyDTO ->
            postAccountWithdrawal(account.id.toString(), money, 400) {
                body(ERROR_MESSAGE_PATH, notNullValue())
            }
        }

        val zeroWithdrawal = MoneyDTO(amount = BigDecimal.valueOf(0, 8))
        assert400WithErrorOnWithdrawal(zeroWithdrawal)

        val negativeWithdrawal = MoneyDTO(amount = BigDecimal.valueOf(-10000, 8))
        assert400WithErrorOnWithdrawal(negativeWithdrawal)
    }

    @Test
    fun `should respond with 400 and message on deposit when body has not positive amount`() {
        val assert400WithErrorOnDeposit = { money: MoneyDTO ->
            postAccountDeposit(account.id.toString(), money, 400) {
                body(ERROR_MESSAGE_PATH, notNullValue())
            }
        }

        val zeroWithdrawal = MoneyDTO(amount = BigDecimal.valueOf(0, 8))
        assert400WithErrorOnDeposit(zeroWithdrawal)

        val negativeWithdrawal = MoneyDTO(amount = BigDecimal.valueOf(-10000, 8))
        assert400WithErrorOnDeposit(negativeWithdrawal)
    }

    @Test
    fun `should respond with 404 and message on deposit when account not found`() {
        val unknownAccountId = UUID.randomUUID()
        val deposit = MoneyDTO(amount = BigDecimal.valueOf(1_50000000, 8))

        postAccountDeposit(unknownAccountId.toString(), deposit, 404) {
            body(ERROR_MESSAGE_PATH, containsStringIgnoringCase(unknownAccountId.toString()))
        }
    }

    @Test
    fun `should respond with 404 and message on withdrawal when account not found`() {
        val unknownAccountId = UUID.randomUUID()
        val withdrawal = MoneyDTO(amount = BigDecimal.valueOf(1_50000000, 8))

        postAccountWithdrawal(unknownAccountId.toString(), withdrawal, 404) {
            body(ERROR_MESSAGE_PATH, containsStringIgnoringCase(unknownAccountId.toString()))
        }
    }
}