package ru.aqrc.project.api.config

import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

object KoinConfig {
    private const val APP_CONFIGURATION_FILENAME = "/application.properties"
    private const val LOGGER_LEVEL_PROPERTY = "logger.level"

    fun init(): Koin = startKoin {
        fileProperties(APP_CONFIGURATION_FILENAME)
        printLogger(
            this.koin
                .getProperty<String>(LOGGER_LEVEL_PROPERTY)
                ?.let(Level::valueOf)
                ?: Level.INFO
        )
    }.koin
}