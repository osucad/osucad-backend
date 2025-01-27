package com.osucad.server.api.config

import com.osucad.server.api.utils.getValue
import io.ktor.server.config.*

class ApiServerConfig(config: ApplicationConfig) {
    val clientUrl: String by config
}