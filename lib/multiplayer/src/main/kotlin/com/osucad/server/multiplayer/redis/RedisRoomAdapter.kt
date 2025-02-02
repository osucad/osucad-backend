package com.osucad.server.multiplayer.redis

import com.osucad.server.multiplayer.BaseRoomAdapter
import com.osucad.server.multiplayer.ILock
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.future.await
import java.util.*
import java.util.concurrent.TimeUnit

class RedisRoomAdapter(
    roomId: UUID,
    redis: RedisRoomResources,
    broadcaster: IMessageBroadcaster,
    meterRegistry: MeterRegistry
) : BaseRoomAdapter(roomId, broadcaster, meterRegistry) {
    override val clients = RedisClientManager(redis)

    override val opsManager = RedisOpsManager(redis)

    private val lock = redis.getLock()

    override suspend fun acquireLock(): ILock {
        val threadId = Thread.currentThread().threadId()

        val res = lock.tryLockAsync(1000, 1000, TimeUnit.MILLISECONDS, threadId)
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