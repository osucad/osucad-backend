package com.osucad.server.messageOrderer

import com.osucad.ktor.redis.Redis
import com.osucad.server.messageOrderer.plugins.configureHealthChecks
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI

fun Application.module() {
    install(Redis)
    configureHealthChecks()

    routing {
        get {
            call.respondText("Hello, world!")
        }
    }
}

private fun createRedisClient(): RedisClient {
    val hostname = System.getenv("REDIS_HOSTNAME") ?: "localhost"
    val port = System.getenv("REDIS_PORT") ?: "6379"
    return RedisClient.create(RedisURI.create(hostname, port.toInt()))
}