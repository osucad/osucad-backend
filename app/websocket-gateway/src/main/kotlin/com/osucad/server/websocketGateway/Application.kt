package com.osucad.server.websocketGateway

import com.osucad.server.websocketGateway.plugins.configureRedis
import com.osucad.server.websocketGateway.plugins.configureWebSockets
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureWebSockets()
    configureRedis()

    routing {
        webSocket("/ws") {
            send("Hello, world!")
        }
    }
}