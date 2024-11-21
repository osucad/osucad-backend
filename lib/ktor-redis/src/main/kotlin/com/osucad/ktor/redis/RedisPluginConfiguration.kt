package com.osucad.ktor.redis

import io.ktor.server.config.*

class RedisPluginConfiguration(config: ApplicationConfig) {
    var hostname: String? = config.tryGetString("hostname")
    var port: Int? = config.tryGetString("port")?.toInt()
}