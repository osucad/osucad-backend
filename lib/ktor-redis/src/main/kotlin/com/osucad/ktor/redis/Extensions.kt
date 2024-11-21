package com.osucad.ktor.redis

import io.ktor.server.application.*
import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

val Application.redisClient get() = attributes[RedisClientAttribute]

@OptIn(ExperimentalContracts::class, ExperimentalLettuceCoroutinesApi::class)
inline fun Application.useRedis(
    block: RedisCoroutinesCommands<String, String>.() -> Unit
) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    redisClient.connect().use { redis ->
        redis.coroutines().block()
    }
}