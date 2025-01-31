package com.osucad.server.multiplayer.redis

import com.osucad.server.multiplayer.types.ClientInfo
import com.osucad.server.multiplayer.types.OpsMessage
import com.osucad.server.multiplayer.types.SummaryMessage
import org.redisson.api.RedissonClient
import java.util.UUID

class RedisRoomResources(
    private val redis: RedissonClient,
    roomId: UUID,
) {
    private val prefix = "osucad:edit:$roomId"

    fun getLock() = redis.getLock("$prefix:lock")

    fun getOpsStream() = redis.getStream<String, OpsMessage>("$prefix:ops")

    fun getSummaryBucket() = redis.getBucket<SummaryMessage>("$prefix:summary")

    fun getClients() = redis.getMap<Long, ClientInfo>("$prefix:clients")

    fun getClientIdGenerator() = redis.getIdGenerator("$prefix:clientIds")
}