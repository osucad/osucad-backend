package com.osucad.server.api.config

import com.osucad.server.api.utils.getValue
import io.ktor.server.config.ApplicationConfig

class RedisConfig(config: ApplicationConfig) {
    val address: String by config
}