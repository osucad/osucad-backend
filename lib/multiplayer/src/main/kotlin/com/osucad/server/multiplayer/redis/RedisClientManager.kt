package com.osucad.server.multiplayer.redis

import com.osucad.server.multiplayer.IClientManager
import com.osucad.server.multiplayer.messages.ServerMessage
import com.osucad.server.multiplayer.types.ClientInfo
import com.osucad.server.multiplayer.types.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.util.*

class RedisClientManager(
    redis: RedisRoomResources,
    private val json: Json = Json
) : IClientManager {
    private val clients = redis.getClients()

    private val idGenerator = redis.getClientIdGenerator()

    private fun nextColor(): String {
        return "#ff0000"
    }

    override suspend fun add(user: UserInfo): ClientInfo {
        val clientId = idGenerator.nextIdAsync()
            .toCompletableFuture()
            .await()

        val clientInfo = RedisClientInfo(
            clientId = clientId,
            user = user,
            color = nextColor(),
            presence = mutableMapOf()
        )

        clients.putAsync(clientId, clientInfo)
            .toCompletableFuture()
            .await()

        return ClientInfo(
            clientId = clientId,
            user = user,
            color = nextColor(),
            presence = mutableMapOf()
        )
    }

    override suspend fun remove(clientId: Long): Boolean {
        return clients.removeAsync(clientId)
            .toCompletableFuture()
            .await() != null
    }

    override suspend fun getAll(): List<ClientInfo> {
        return clients.readAllValuesAsync()
            .toCompletableFuture()
            .await()
            .toList()
            .map {
                ClientInfo(
                    clientId = it.clientId,
                    user = it.user,
                    color = it.color,
                    presence = it.presence.mapValues { (key, value) ->
                        json.decodeFromString<JsonElement>(value)
                    }
                )
            }
    }

    override suspend fun updatePresence(clientId: Long, key: String, value: JsonElement) {
        val client = clients.getAsync(clientId)
            .toCompletableFuture()
            .await()

        client.presence = client.presence
            .toMutableMap()
            .also { it[key] = json.encodeToString(value) }

        clients.putIfExistsAsync(clientId, client)
            .toCompletableFuture()
            .await()
    }
}

interface IMessageBroadcaster {
    suspend fun broadcast(roomId: UUID, message: ServerMessage)

    fun incoming(roomId: UUID): Flow<ServerMessage>
}