package com.osucad.server.websocketGateway

import com.osucad.server.websocketGateway.plugins.configureRedis
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureRedis()

    routing {
        get {
            call.respond("Hello, world!")
        }
    }
}