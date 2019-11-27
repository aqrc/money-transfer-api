package ru.aqrc.project.api.config

import io.javalin.Javalin
import org.koin.core.KoinComponent
import org.koin.core.context.stopKoin
import ru.aqrc.project.api.web.Router

object AppConfig : KoinComponent {
    private const val SERVER_PORT_PROPERTY = "server.port"
    private const val DEFAULT_PORT = 7000

    fun startApplication(): Javalin =
        Javalin
            .create()
            .events { event ->
                event.serverStopping { stopKoin() }
            }
            .routes(Router.endpoints())
            .start(getKoin().getProperty(SERVER_PORT_PROPERTY, DEFAULT_PORT))
}
