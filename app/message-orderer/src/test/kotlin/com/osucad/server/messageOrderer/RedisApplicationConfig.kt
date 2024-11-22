package com.osucad.server.messageOrderer

import com.redis.testcontainers.RedisContainer
import io.ktor.server.config.*

class RedisApplicationConfig(container: RedisContainer) : MapApplicationConfig(
    "osucad.redis.hostname" to container.redisHost,
    "osucad.redis.port" to "${container.redisPort}",
)