package com.osucad.server.multiplayer.local

import com.osucad.server.multiplayer.redis.IMessageBroadcaster
import com.osucad.server.multiplayer.messages.ServerMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.util.UUID

class LocalMessageBroadcaster : IMessageBroadcaster {
    private val sharedMutableFlow = MutableSharedFlow<Pair<UUID, ServerMessage>>(extraBufferCapacity = 100)

    override suspend fun broadcast(
        roomId: UUID,
        message: ServerMessage
    ) {
        sharedMutableFlow.emit(roomId to message)
    }

    override fun incoming(roomId: UUID): Flow<ServerMessage> {
        return sharedMutableFlow
            .filter { (id, _) -> id == roomId }
            .map { (_, message) -> message }
    }
}