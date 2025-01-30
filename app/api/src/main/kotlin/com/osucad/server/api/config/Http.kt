package com.osucad.server.api.config

import com.osucad.server.api.exceptions.ResponseStatusException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<ResponseStatusException> { call, cause ->
            call.respond(cause.status, cause.message ?: cause.status.description)
            throw cause
        }
        exception<Exception> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, "Internal server error")
            throw cause
        }
    }

    if (developmentMode) {
        install(CORS) {
            anyHost()
            anyMethod()
            allowCredentials = true
        }
    }
}