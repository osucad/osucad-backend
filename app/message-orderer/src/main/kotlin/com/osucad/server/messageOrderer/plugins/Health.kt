package com.osucad.server.messageOrderer.plugins

import com.osucad.ktor.redis.redisClient
import dev.hayden.KHealth
import io.ktor.server.application.*
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.api.coroutines

@OptIn(ExperimentalLettuceCoroutinesApi::class)
fun Application.configureHealthChecks() {
    val redisConnection = redisClient.connect().coroutines()

    install(KHealth) {
        readyChecks {
            check("redis") {
                runCatching { redisConnection.ping() }.isSuccess
            }
        }
        healthChecks {
            check("redis") {
                runCatching { redisConnection.ping() }.isSuccess
            }
        }
    }
}