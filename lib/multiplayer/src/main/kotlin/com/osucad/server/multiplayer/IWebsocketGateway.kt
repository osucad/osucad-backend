package com.osucad.server.multiplayer

import com.osucad.server.multiplayer.types.UserInfo
import java.util.UUID

interface IWebsocketGateway {
    suspend fun accept(socket: IWebSocket, roomId: UUID, user: UserInfo)
}