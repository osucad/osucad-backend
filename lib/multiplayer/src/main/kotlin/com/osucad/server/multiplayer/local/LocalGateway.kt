package com.osucad.server.multiplayer.local

import com.osucad.server.multiplayer.IWebSocket
import com.osucad.server.multiplayer.IWebsocketGateway
import com.osucad.server.multiplayer.types.UserInfo
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import java.util.Collections
import java.util.UUID

class LocalGateway(private val meterRegistry: MeterRegistry) : IWebsocketGateway {
    private val logger = LoggerFactory.getLogger(LocalGateway::class.java)

    init {
        logger.warn("LocalGateway is intended for development only, please use RedisGateway in production")
    }

    private val clientManagers = Collections.synchronizedMap(mutableMapOf<UUID, LocalClientManager>())
    private val opsManagers = Collections.synchronizedMap(mutableMapOf<UUID, LocalOpsManager>())
    private val broadcaster = LocalMessageBroadcaster()

    override suspend fun accept(socket: IWebSocket, roomId: UUID, user: UserInfo) {
        val clientManager = clientManagers.computeIfAbsent(roomId) { LocalClientManager() }
        val opsManager = opsManagers.computeIfAbsent(roomId) { LocalOpsManager() }

        val room = LocalRoomAdapter(roomId, broadcaster, meterRegistry, clientManager, opsManager)

        room.handle(socket, user)
    }
}