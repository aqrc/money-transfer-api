package ru.aqrc.project.api.web.controller

import kotlinx.coroutines.*
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConcurrentTransfersTest {
    private lateinit var fromUser: UserDTO
    private lateinit var fromAccount: AccountDTO
    private lateinit var toUser: UserDTO
    private lateinit var toAccount: AccountDTO

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

    @BeforeEach
    fun setUp() {
        fromUser = postUser(UserDTO(name = "User From"))!!
        fromAccount = postUserAccount(fromUser.id.toString())!!

        toUser = postUser(UserDTO(name = "User To"))!!
        toAccount = postUserAccount(toUser.id.toString())!!
    }

    @Test
    fun `should not lose money on concurrent requests`() {
        val concurrency = 6
        val depositPart = 500L

        val deposit = MoneyDTO(amount = BigDecimal.valueOf(depositPart * concurrency, 8))
        postAccountDeposit(fromAccount.id.toString(), deposit) {
            body(AMOUNT_PATH, equalTo(deposit.amount))
        }

        val transfers = mutableListOf<Deferred<Unit?>>()
        val smallMoney = MoneyDTO(amount = BigDecimal.valueOf(depositPart, 8))
        for (i in 1..concurrency) {
            transfers.add(
                GlobalScope.async(start = CoroutineStart.LAZY) {
                    postTransfer(fromAccount.id.toString(), toAccount.id.toString(), smallMoney)
                        .also { println("$i finished") }
                }
            )
        }

        runBlocking {
            transfers.awaitAll()
        }

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