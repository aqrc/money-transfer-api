package ru.aqrc.project.api.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.javalin.Javalin
import io.javalin.core.validation.JavalinValidation
import io.javalin.plugin.json.JavalinJackson
import org.h2.tools.Server
import org.koin.core.KoinComponent
import org.koin.core.context.stopKoin
import org.koin.core.inject
import ru.aqrc.project.api.web.Router
import java.util.*
import ru.aqrc.project.api.model.database.IDatabaseInitializer
import ru.aqrc.project.api.web.ExceptionMapper

object AppConfig : KoinComponent {
    private val router: Router by inject()
    private val databaseInitializer: IDatabaseInitializer by inject()
    private lateinit var dbServer: Server

    private const val SERVER_PORT_PROPERTY = "server.port"
    private const val DEFAULT_PORT = 7000

    fun startApplication(): Javalin =
        Javalin
            .create()
            .events { event ->
                event.serverStarting {
                    configureObjectMapper()
                    configureValidator()
                    startDbServer()
                    databaseInitializer.initDatabase()
                }
                event.serverStopping {
                    stopKoin()
                    stopDbServer()
                }
            }
            .routes(router.endpoints())
            .start(getPort())
            .also(::addShutdownHook)
            .also(ExceptionMapper::configure)

    private fun configureObjectMapper() {
        JavalinJackson.configure(
            jacksonObjectMapper()
                .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
        )
    }

    private fun configureValidator() {
        JavalinValidation.register(UUID::class.java, UUID::fromString)
    }

    private fun startDbServer() {
        dbServer = Server.createWebServer().start()
    }

    private fun stopDbServer() = dbServer.stop()

    private fun getPort() = getKoin().getProperty(SERVER_PORT_PROPERTY, DEFAULT_PORT)

    private fun addShutdownHook(app: Javalin) {
        Runtime.getRuntime().addShutdownHook(Thread { app.stop() })
    }
}
