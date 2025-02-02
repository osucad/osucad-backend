package com.osucad.server.multiplayer.redis

import com.osucad.server.multiplayer.IWebSocket
import com.osucad.server.multiplayer.IWebsocketGateway
import com.osucad.server.multiplayer.types.UserInfo
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.launch
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

class RedisGateway(
    private val redis: RedissonClient,
    private val meterRegistry: MeterRegistry = SimpleMeterRegistry(),
) : IWebsocketGateway {
    private val logger = LoggerFactory.getLogger(javaClass)

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

        try {
            room.handle(socket, user)
        } catch (e: ClosedReceiveChannelException) {
            // client disconnected, nothing to do
        } catch (e: ClosedSendChannelException) {
            // client disconnected, nothing to do
        }
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