package com.osucad.server.multiplayer.redis

import com.osucad.server.multiplayer.IWebSocket
import com.osucad.server.multiplayer.IWebsocketGateway
import com.osucad.server.multiplayer.types.UserInfo
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.redisson.api.RedissonClient
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

class RedisGateway(
    private val redis: RedissonClient,
    private val meterRegistry: MeterRegistry = SimpleMeterRegistry(),
) : IWebsocketGateway {
    private val broadcaster = RedisMessageBroadcaster(redis, meterRegistry)

    override suspend fun accept(
        socket: IWebSocket,
        roomId: UUID,
        user: UserInfo
    ) {
        ensureInitialized()


        val room = RedisRoomAdapter(
            roomId = roomId,
            redis = RedisRoomResources(redis, roomId),
            broadcaster = broadcaster,
            meterRegistry = meterRegistry,
        )

        room.handle(socket, user)
    }

    private val initialized = AtomicBoolean(false)


    private fun ensureInitialized() {
        if (!initialized.getAndSet(true))
            initialize()
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun initialize() {
        GlobalScope.launch {
            broadcaster.beginPolling()
        }
    }
}