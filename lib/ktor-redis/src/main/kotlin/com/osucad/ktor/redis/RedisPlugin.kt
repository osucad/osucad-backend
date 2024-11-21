package com.osucad.ktor.redis

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.util.*
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI

val RedisClientAttribute = AttributeKey<RedisClient>("osucad.redis-client")

val Redis = createApplicationPlugin(
    name = "osucad.redis",
    configurationPath = "osucad.redis",
    createConfiguration = ::RedisPluginConfiguration
) {
    val redisClient = createRedisClient(pluginConfig)

    application.attributes.put(RedisClientAttribute, redisClient)
}


private fun createRedisClient(config: RedisPluginConfiguration): RedisClient {
    val hostname = config.hostname ?: throw ApplicationConfigurationException("hostname is missing")
    val port = config.port ?: throw ApplicationConfigurationException("port is missing")

    val redisUri = RedisURI.create(hostname, port)

    return RedisClient.create(redisUri)
}