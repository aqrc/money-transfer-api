package ru.aqrc.project.api

import ru.aqrc.project.api.config.AppConfig
import ru.aqrc.project.api.config.KoinConfig

fun main(args: Array<String>) {
    KoinConfig.init()
    AppConfig.startApplication()
}