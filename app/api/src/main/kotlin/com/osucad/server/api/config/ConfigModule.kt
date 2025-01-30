package com.osucad.server.api.config

import io.ktor.server.config.ApplicationConfig
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class ConfigModule {
    @Single
    fun apiServerConfig(config: ApplicationConfig) = OsucadEndpointConfig(config.config("osucad.endpoints"))

    @Single
    fun sessionConfig(config: ApplicationConfig) = SessionConfig(config.config("session"))

    @Single
    fun osuOauthConfig(config: ApplicationConfig) = OsuOAuthConfig(config.config("oauth.clients.osu"))
}