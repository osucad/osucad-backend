package com.osucad.server.api.config

import com.osucad.server.api.utils.getValue
import io.ktor.server.config.*

class OsucadEndpointConfig(config: ApplicationConfig) {
    val client: String by config
    val beatmapParser: String by config
}