package com.osucad.server.websocketGateway

import io.ktor.server.config.ApplicationConfig
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config

fun createRedis(applicationConfig: ApplicationConfig): RedissonClient {
    val config = Config()
    config.useSingleServer()
        .setAddress(applicationConfig.property("redis.address").getString())

    val redis = Redisson.create()

    return redis
}