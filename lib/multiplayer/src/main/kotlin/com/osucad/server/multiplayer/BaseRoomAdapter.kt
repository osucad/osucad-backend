package com.osucad.server.multiplayer

import com.osucad.server.multiplayer.messages.ClientMessage
import com.osucad.server.multiplayer.messages.ServerMessage
import com.osucad.server.multiplayer.redis.IMessageBroadcaster
import com.osucad.server.multiplayer.types.ClientInfo
import com.osucad.server.multiplayer.types.SequenceNumber
import com.osucad.server.multiplayer.types.SummaryWithOps
import com.osucad.server.multiplayer.types.UserInfo
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.Timer
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*

abstract class BaseRoomAdapter(
    protected val roomId: UUID,
    protected val broadcaster: IMessageBroadcaster,
    protected val meterRegistry: MeterRegistry,
) {
    protected abstract val clients: IClientManager
    protected abstract val opsManager: IOpsManager

    protected abstract suspend fun acquireLock(): ILock

    private val incomingOpsCounter = Counter.builder("ops.incoming")
        .description("Number of messages sent to the ops channel")
        .withRegistry(meterRegistry)

    private val outgoingOpsCounter = Counter.builder("ops.outgoing")
        .description("Number of messages sent to the ops channel")
        .withRegistry(meterRegistry)

    private val submitOpsTimer = Timer.builder("ops.submit")
        .description("Time spent in submitting ops")
        .publishPercentiles(0.5, 0.9, 0.99)
        .withRegistry(meterRegistry)

    suspend fun handle(socket: IWebSocket, userInfo: UserInfo) {
        var clientInfo: ClientInfo? = null

        try {
            val client: RoomClient
            val lastSequenceNumber: SequenceNumber

            acquireLock().use {
                clientInfo = addUserToRoom(userInfo)

                client = RoomClient(socket, clientInfo!!)

                lastSequenceNumber = sendInitialState(client)
            }

            coroutineScope {
                launch(Job()) {
                    receiveBroadcastMessages(client)
                }

                launch(Job()) {
                    receiveOps(client, lastSequenceNumber)
                }
            }

            handleClientMessages(client)
        } finally {
            if (clientInfo != null)
                removeUserFromRoom(clientInfo!!)
        }
    }

    private suspend fun sendInitialState(client: RoomClient): SequenceNumber {

        val summary = opsManager.getSummary()
        val ops = opsManager.getOpsSince(summary.sequenceNumber)
        val connectedUsers = clients.getAll()

        val message = ServerMessage.InitialState(
            clientId = client.client.clientId,
            document = SummaryWithOps(summary, ops),
            connectedUsers = connectedUsers,
        )

        client.socket.send(message)

        return ops.lastOrNull()?.sequenceNumber ?: summary.sequenceNumber
    }

    protected open suspend fun addUserToRoom(user: UserInfo): ClientInfo {
        val client = clients.add(user)
        broadcaster.broadcast(roomId, ServerMessage.UserJoined(client))

        return client
    }

    protected open suspend fun removeUserFromRoom(client: ClientInfo) {
        acquireLock().use {
            clients.remove(client.clientId)
            broadcaster.broadcast(roomId, ServerMessage.UserLeft(client))
        }
    }

    private suspend fun receiveBroadcastMessages(client: RoomClient) =
        broadcaster.incoming(roomId).collect { message ->
            client.send(message)
        }

    private suspend fun receiveOps(client: RoomClient, initialSequenceNumber: SequenceNumber) {
        val tags = tags(client)

        opsManager.incoming(initialSequenceNumber).collect { ops ->
            client.send(ServerMessage.OpsSubmitted(ops))

            outgoingOpsCounter.withTags(tags).increment(ops.size.toDouble())
        }
    }

    private suspend fun handleClientMessages(client: RoomClient) {
        client.socket.incoming().collect { message ->
            handleMessage(client, message)
        }
    }

    private suspend fun handleMessage(client: RoomClient, message: ClientMessage) {
        when (message) {
            is ClientMessage.SubmitOps -> submitOps(client, message)
            is ClientMessage.UpdatePresence -> updatePresence(client, message)
        }
    }

    private suspend fun submitOps(client: RoomClient, message: ClientMessage.SubmitOps) {
        val tags = tags(client)

        val sample = Timer.start()
        opsManager.append(client.clientId, message.version, message.ops)
        sample.stop(submitOpsTimer.withTags(tags))

        outgoingOpsCounter.withTags(tags).increment()
    }

    private suspend fun updatePresence(client: RoomClient, message: ClientMessage.UpdatePresence) {
        val newPresence = client.client.presence
            .toMutableMap()
            .also { it[message.key] = message.value }

        client.client.presence = newPresence

        clients.updatePresence(client.clientId, message.key, message.value)

        broadcaster.broadcast(roomId, ServerMessage.PresenceUpdated(client.clientId, message.key, message.value))
    }

    private fun tags(client: RoomClient) = Tags.of(
        "room", roomId.toString(),
        "client", client.clientId.toString(),
    )
}