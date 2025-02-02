package com.osucad.server.multiplayer

import com.osucad.server.multiplayer.types.ClientInfo
import com.osucad.server.multiplayer.types.UserInfo
import kotlinx.serialization.json.JsonElement

interface IClientManager {
    suspend fun add(user: UserInfo): ClientInfo

    suspend fun remove(clientId: Long): Boolean

    suspend fun getAll(): List<ClientInfo>

    suspend fun updatePresence(clientId: Long, key: String, value: JsonElement)
}