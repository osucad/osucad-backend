package com.osucad.server.api.config

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

fun Application.configureLogging() {
    install(CallLogging) {
        level = Level.DEBUG
        filter { call -> !call.request.path().startsWith("/api/v1/auth") }
    }
}