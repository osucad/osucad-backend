package com.osucad.server.websocketGateway

import com.osucad.server.multiplayer.messages.ClientMessage
import com.osucad.server.multiplayer.messages.ServerMessage
import com.osucad.server.multiplayer.IWebSocket
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

val json = Json { encodeDefaults = true }

class KtorWebSocketAdapter(private val session: DefaultWebSocketServerSession) : IWebSocket {
    override suspend fun send(message: ServerMessage) {
        session.sendSerialized(message)
    }

    override suspend fun close() {
        session.close()
    }

    override fun incoming(): Flow<ClientMessage> {
        return flow {
            while (true) {
                val message = session.receiveDeserialized<ClientMessage>()

                emit(message)
            }
        }
    }
}