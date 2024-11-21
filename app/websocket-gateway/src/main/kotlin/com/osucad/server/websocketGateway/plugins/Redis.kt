package com.osucad.server.websocketGateway.plugins

import com.osucad.ktor.redis.Redis
import io.ktor.server.application.*

fun Application.configureRedis() {
    install(Redis)
}
