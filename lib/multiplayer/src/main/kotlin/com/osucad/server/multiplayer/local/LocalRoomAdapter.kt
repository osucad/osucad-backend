package com.osucad.server.multiplayer.local

import com.osucad.server.multiplayer.BaseRoomAdapter
import com.osucad.server.multiplayer.ILock
import com.osucad.server.multiplayer.redis.IMessageBroadcaster
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.sync.Mutex
import java.util.*

class LocalRoomAdapter(
    roomId: UUID,
    broadcaster: IMessageBroadcaster,
    meterRegistry: MeterRegistry,
    override val clients: LocalClientManager,
    override val opsManager: LocalOpsManager,
) : BaseRoomAdapter(roomId, broadcaster, meterRegistry) {
    companion object {
        private val mutex = Mutex()
    }

    override suspend fun acquireLock(): ILock {
        val owner = this

        mutex.lock(owner)

        return object : ILock {
            override suspend fun release() {
                mutex.unlock(owner)
            }
        }
    }
}