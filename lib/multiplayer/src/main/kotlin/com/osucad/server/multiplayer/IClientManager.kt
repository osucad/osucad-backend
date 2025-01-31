package com.osucad.server.multiplayer

import com.osucad.server.multiplayer.types.ClientInfo
import com.osucad.server.multiplayer.types.UserInfo

interface IClientManager {
    suspend fun add(user: UserInfo): ClientInfo

    suspend fun remove(clientId: Long): Boolean

    suspend fun getAll(): List<ClientInfo>
}