package com.osucad.server.api

import com.osucad.server.api.config.configureDatabase
import com.osucad.server.api.config.configureHttp
import com.osucad.server.api.config.configureKoin
import com.osucad.server.api.config.configureLogging
import com.osucad.server.api.config.configureRouting
import com.osucad.server.api.config.configureSecurity
import com.osucad.server.api.config.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    configureLogging()
    configureKoin()
    configureHttp()
    configureSerialization()
    configureDatabase()
    configureSecurity()
    configureRouting()
}