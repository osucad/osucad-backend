package com.osucad.server.api.config

import io.ktor.server.config.*
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
class ConfigModule {
    @Single
    fun apiServerConfig(config: ApplicationConfig) = OsucadEndpointConfig(config.config("osucad.endpoints"))


    @Single
    fun sessionConfig(config: ApplicationConfig) = SessionConfig(config.config("session"))


    @Single
    fun osuOauthConfig(config: ApplicationConfig) = OsuOAuthConfig(config.config("oauth.clients.osu"))


    @Single
    fun redisConfig(config: ApplicationConfig) = RedisConfig(config.config("redis"))


    @Single
    @Named("multiplayerJwtConfig")
    fun jwtConfig(config: ApplicationConfig): JwtConfig = JwtConfig(config.config("osucad.jwt.multiplayer"))
}