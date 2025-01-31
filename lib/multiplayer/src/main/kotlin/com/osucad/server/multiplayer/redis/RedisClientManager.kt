package com.osucad.server.multiplayer.redis

import com.osucad.server.multiplayer.IClientManager
import com.osucad.server.multiplayer.messages.ServerMessage
import com.osucad.server.multiplayer.types.ClientInfo
import com.osucad.server.multiplayer.types.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.future.await
import org.redisson.api.RedissonClient
import java.util.UUID

class RedisClientManager(
    redis: RedisRoomResources,
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

        val clientInfo = ClientInfo(
            clientId = clientId,
            user = user,
            color = nextColor(),
        )

        clients.putAsync(clientId, clientInfo)
            .toCompletableFuture()
            .await()

        return clientInfo
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
    }
}

interface IMessageBroadcaster {
    suspend fun broadcast(roomId: UUID, message: ServerMessage)

    fun incoming(roomId: UUID): Flow<ServerMessage>
}