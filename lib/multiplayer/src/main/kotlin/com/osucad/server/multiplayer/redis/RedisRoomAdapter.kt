package com.osucad.server.multiplayer.redis

import com.osucad.server.multiplayer.BaseRoomAdapter
import com.osucad.server.multiplayer.ILock
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.future.await
import org.redisson.api.RedissonClient
import java.util.*
import java.util.concurrent.TimeUnit

class RedisRoomAdapter(
    roomId: UUID,
    redis: RedissonClient,
    broadcaster: IMessageBroadcaster,
    meterRegistry: MeterRegistry
) : BaseRoomAdapter(roomId, broadcaster, meterRegistry) {
    private val prefix = "osucad:edit:$roomId"

    override val clients = ClientManager(redis, prefix)

    override val opsManager = RedisOpsManager(redis, prefix)

    private val lock = redis.getLock("$prefix:lock")

    override suspend fun acquireLock(): ILock {
        val threadId = Thread.currentThread().threadId()

        val res = lock.tryLockAsync(100, 100, TimeUnit.MILLISECONDS, threadId)
            .toCompletableFuture()
            .await()

        if (!res)
            throw IllegalStateException("Could not acquire lock")

        var released = false



        return object : ILock {
            override suspend fun release() {
                if (!released) {
                    lock.unlockAsync(threadId)
                        .toCompletableFuture()
                        .await()

                    released = true
                }
            }
        }
    }
}