package com.osucad.server.multiplayer.local

import com.osucad.server.multiplayer.IWebSocket
import com.osucad.server.multiplayer.messages.ClientMessage
import com.osucad.server.multiplayer.messages.ServerMessage
import com.osucad.server.multiplayer.types.SequenceNumber
import com.osucad.server.multiplayer.types.UserInfo
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import java.util.*

class LocalRoomAdapterTest : AnnotationSpec() {
    @OptIn(DelicateCoroutinesApi::class)
    @Test
    suspend fun testLocalRoomAdapter() {
        val roomId = UUID.randomUUID()
        val broadcaster = LocalMessageBroadcaster()
        val opsManager = LocalOpsManager()
        val clientManager = LocalClientManager()
        val meterRegistry = SimpleMeterRegistry()

        opsManager.appendSummary(0, SequenceNumber(0), "", emptyList())

        val adapter = LocalRoomAdapter(roomId, broadcaster, meterRegistry, clientManager, opsManager)

        val socket = TestWebSocket()

        coroutineScope {
            val job = Job()

            launch(job) {
                adapter.handle(socket, UserInfo(0, "guest"))
            }

            socket.outgoing.receive().also { message ->
                message.shouldBeInstanceOf<ServerMessage.InitialState>()
                message.connectedUsers shouldHaveSize 1
                message.document.summary shouldBe opsManager.getSummary()
            }

            socket.send(ClientMessage.SubmitOps(0, listOf()))

            socket.outgoing.receive().also { message ->
                message.shouldBeInstanceOf<ServerMessage.OpsSubmitted>()
                message.ops shouldHaveSize 1
            }

            job.cancel()
        }
    }


    @Test
    suspend fun testJoinLeaveMessages() {
        val roomId = UUID.randomUUID()
        val broadcaster = LocalMessageBroadcaster()
        val opsManager = LocalOpsManager()
        val clientManager = LocalClientManager()
        val meterRegistry = SimpleMeterRegistry()
        val adapter = LocalRoomAdapter(roomId, broadcaster, meterRegistry, clientManager, opsManager)

        opsManager.appendSummary(0, SequenceNumber(0), "", emptyList())

        val socket1 = TestWebSocket()
        val socket2 = TestWebSocket()

        coroutineScope {
            val job = Job()

            launch(job) {
                adapter.handle(socket1, UserInfo(0, "user 1"))
            }

            socket1.outgoing.receive()

            launch(job) {
                adapter.handle(socket2, UserInfo(1, "user 2"))
            }

            socket1.outgoing.receive().also { message ->
                message.shouldBeInstanceOf<ServerMessage.UserJoined>()
                message.client.user.username shouldBe "user 2"
            }

            socket2.close()

            socket1.outgoing.receive().also { message ->
                message.shouldBeInstanceOf<ServerMessage.UserLeft>()
                message.client.user.username shouldBe "user 2"
            }

            job.cancel()
        }
    }

    class TestWebSocket : IWebSocket {
        val incoming = Channel<ClientMessage>(Channel.UNLIMITED)
        val outgoing = Channel<ServerMessage>(Channel.UNLIMITED)

        override suspend fun send(message: ServerMessage) {
            outgoing.send(message)
        }

        suspend fun send(message: ClientMessage) {
            incoming.send(message)
        }

        override fun incoming(): Flow<ClientMessage> {
            return incoming.consumeAsFlow()
        }

        override suspend fun close() {
            outgoing.close()
            incoming.close()
        }
    }
}