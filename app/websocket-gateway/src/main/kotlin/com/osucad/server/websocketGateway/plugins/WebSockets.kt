package com.osucad.server.websocketGateway.plugins

import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

fun Application.configureWebSockets() {
    val isDevelopmentMode = developmentMode

    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json {
            prettyPrint = isDevelopmentMode
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }
}