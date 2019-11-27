package ru.aqrc.project.api.config

import io.javalin.Javalin
import io.javalin.core.validation.JavalinValidation
import org.koin.core.KoinComponent
import org.koin.core.context.stopKoin
import org.koin.core.inject
import ru.aqrc.project.api.web.Router
import java.util.*

object AppConfig : KoinComponent {
    private val router: Router by inject()

    private const val SERVER_PORT_PROPERTY = "server.port"
    private const val DEFAULT_PORT = 7000

    // TODO add error mapping
    fun startApplication(): Javalin =
        Javalin
            .create()
            .events { event ->
                event.serverStarting {
                    JavalinValidation.register(UUID::class.java, UUID::fromString)
                }
                event.serverStopping { stopKoin() }
            }
            .routes(router.endpoints())
            .start(getKoin().getProperty(SERVER_PORT_PROPERTY, DEFAULT_PORT))
}
