package com.osucad.server.multiplayer

import com.osucad.server.multiplayer.messages.ServerMessage
import com.osucad.server.multiplayer.types.ClientInfo

internal class RoomClient(
    val socket: IWebSocket,
    val client: ClientInfo,
) {
    val clientId get() = client.clientId

    suspend fun send(message: ServerMessage) {
        socket.send(message)
    }
}