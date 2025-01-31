package com.osucad.server.api.config

import com.osucad.server.api.utils.getValue
import io.ktor.server.config.ApplicationConfig

class JwtConfig(config: ApplicationConfig) {
    val secret: String by config
    val issuer: String by config
    val audience: String by config
}