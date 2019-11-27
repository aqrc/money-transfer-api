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

object AppConfig : KoinComponent {
    private val router: Router by inject()
    private val databaseInitializer: IDatabaseInitializer by inject()
    private lateinit var dbServer: Server

    private const val SERVER_PORT_PROPERTY = "server.port"
    private const val DEFAULT_PORT = 7000

    // TODO add error mapping
    fun startApplication(): Javalin =
        Javalin
            .create()
            .events { event ->
                event.serverStarting {
                    configureObjectMapper()
                    configureValidator()
                    dbServer = Server.createWebServer().start()
                    databaseInitializer.initDatabase()
                }
                event.serverStopping {
                    stopKoin()
                    dbServer.stop()
                }
            }
            .routes(router.endpoints())
            .start(getKoin().getProperty(SERVER_PORT_PROPERTY, DEFAULT_PORT))
            .also { app ->
                Runtime.getRuntime().addShutdownHook(Thread { app.stop() })
            }

    private fun configureObjectMapper() {
        JavalinJackson.configure(
            jacksonObjectMapper()
                .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
        )
    }

    private fun configureValidator() {
        JavalinValidation.register(UUID::class.java, UUID::fromString)
    }
}
