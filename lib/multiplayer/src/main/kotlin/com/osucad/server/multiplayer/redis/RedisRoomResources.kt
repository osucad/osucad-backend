package com.osucad.server.multiplayer.redis

import com.osucad.server.multiplayer.types.OpsMessage
import com.osucad.server.multiplayer.types.SummaryMessage
import kotlinx.serialization.json.Json
import org.redisson.api.RBucket
import org.redisson.api.RIdGenerator
import org.redisson.api.RLock
import org.redisson.api.RMap
import org.redisson.api.RStream
import org.redisson.api.RedissonClient
import java.util.UUID

class RedisRoomResources(
    private val redis: RedissonClient,
    roomId: UUID,
    private val json: Json = Json
) {
    private val prefix = "osucad:edit:$roomId"

    fun getLock(): RLock = redis.getLock("$prefix:lock")

    fun getOpsStream(): RStream<String, OpsMessage> = redis.getStream("$prefix:ops")

    fun getSummaryBucket(): RBucket<SummaryMessage> = redis.getBucket("$prefix:summary")

    fun getClients(): RMap<Long, RedisClientInfo> = redis.getMap("$prefix:clients")

    fun getClientIdGenerator(): RIdGenerator = redis.getIdGenerator("$prefix:clientIds")
}