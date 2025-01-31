package com.osucad.server.multiplayer.redis

import com.osucad.server.multiplayer.messages.ServerMessage
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.redisson.api.RedissonClient
import org.redisson.api.StreamMessageId
import org.redisson.api.stream.StreamAddArgs
import org.redisson.api.stream.StreamReadArgs
import java.time.Duration
import java.util.*

private const val CHANNEL_NAME = "osucad:ws:broadcast"
private const val MESSAGE_KEY = "message"

private val json = Json { encodeDefaults = true }

class RedisMessageBroadcaster(redis: RedissonClient, private val meterRegistry: MeterRegistry) : IMessageBroadcaster {
    private val sharedMutableFlow = MutableSharedFlow<BroadcastEvent>(extraBufferCapacity = 100)

    private val stream = redis.getStream<String, String>(CHANNEL_NAME)

    private val pollTimer = Timer.builder("broadcast.poll")
        .description("Time spent in polling new messages to broadcast")
        .register(meterRegistry)

    private val incomingMessageCounter = Counter.builder("broadcast.messages.incoming")
        .description("Number of messages sent to the broadcast channel")
        .withRegistry(meterRegistry)

    private val outgoingMessageCounter = Counter.builder("broadcast.messages.outgoing")
        .description("Number of messages sent to the broadcast channel")
        .withRegistry(meterRegistry)


    override suspend fun broadcast(roomId: UUID, message: ServerMessage) {
        append(roomId, BroadcastEvent.SendMessage(roomId.toString(), message))
    }


    suspend fun beginPolling() {
        var lastId = StreamMessageId.NEWEST

        while (true) {
            val args = StreamReadArgs.greaterThan(lastId).timeout(Duration.ofMillis(10))

            val sample = Timer.start(meterRegistry)

            val messages = stream.read(args)

            sample.stop(pollTimer)

            for ((id, message) in messages) {
                val event = json.decodeFromString(BroadcastEvent.serializer(), message[MESSAGE_KEY]!!)

                incomingMessageCounter.withTag("room", event.room).increment()

                when (event) {
                    is BroadcastEvent.SendMessage -> sharedMutableFlow.emit(event)
                }

                lastId = id
            }
        }
    }

    override fun incoming(roomId: UUID): Flow<ServerMessage> {
        val idAsString = roomId.toString()

        return sharedMutableFlow.asSharedFlow()
            .mapNotNull { evt ->
                if (evt is BroadcastEvent.SendMessage && evt.room == idAsString) {
                    outgoingMessageCounter
                        .withTag("room", idAsString)
                        .increment()

                    evt.message
                } else {
                    null
                }
            }
    }

    private suspend fun append(roomId: UUID, message: BroadcastEvent) {
        val encoded = json.encodeToString(message)

        stream.add(StreamAddArgs.entry(MESSAGE_KEY, encoded))
    }


    @Serializable
    sealed interface BroadcastEvent {
        val room: String


        @Serializable
        data class SendMessage(override val room: String, val message: ServerMessage) : BroadcastEvent
    }
}