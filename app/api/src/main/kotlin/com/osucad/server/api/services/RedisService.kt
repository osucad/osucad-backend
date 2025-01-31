package com.osucad.server.api.services

import com.osucad.server.api.config.RedisConfig
import org.koin.core.annotation.Single
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config

@Single
class RedisService(redisConfig: RedisConfig) {
    val redis: RedissonClient

    init {
        val config = Config()
        config.useSingleServer().setAddress(redisConfig.address)

        redis = Redisson.create(config)
    }
}