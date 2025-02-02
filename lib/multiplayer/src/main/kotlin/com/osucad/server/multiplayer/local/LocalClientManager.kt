package com.osucad.server.multiplayer.local

import com.osucad.server.multiplayer.IClientManager
import com.osucad.server.multiplayer.types.ClientInfo
import com.osucad.server.multiplayer.types.UserInfo
import kotlinx.serialization.json.JsonElement
import java.util.Collections
import java.util.concurrent.atomic.AtomicLong

class LocalClientManager : IClientManager {
    private val clients = Collections.synchronizedList(mutableListOf<ClientInfo>())

    private val clientsIds = AtomicLong(0)

    private fun nextClientId() = clientsIds.incrementAndGet()

    override suspend fun add(user: UserInfo): ClientInfo {
        val clientId = nextClientId()

        val clientInfo = ClientInfo(
            clientId = clientId,
            user = user,
            color = "#ff0000",
            presence = mutableMapOf()
        )

        clients.add(clientInfo)

        return clientInfo
    }

    override suspend fun remove(clientId: Long): Boolean {
        return clients.removeIf { it.clientId == clientId }
    }

    override suspend fun getAll(): List<ClientInfo> {
        return clients.toList()
    }

    override suspend fun updatePresence(clientId: Long, key: String, value: JsonElement) {
        // Nothing to do here since room adapter already handled it
    }
}