package com.osucad.server.multiplayer

import com.osucad.server.multiplayer.messages.ClientMessage
import com.osucad.server.multiplayer.messages.ServerMessage
import kotlinx.coroutines.flow.Flow

interface IWebSocket {
    suspend fun send(message: ServerMessage)

    suspend fun close()

    fun incoming(): Flow<ClientMessage>
}