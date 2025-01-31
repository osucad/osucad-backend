package com.osucad.server.multiplayer.redis

import com.osucad.server.multiplayer.IOpsManager
import com.osucad.server.multiplayer.types.OpsMessage
import com.osucad.server.multiplayer.types.SequenceNumber
import com.osucad.server.multiplayer.types.SequencedOpsMessage
import com.osucad.server.multiplayer.types.SummaryMessage
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.future.await
import org.redisson.api.StreamMessageId
import org.redisson.api.stream.StreamAddArgs
import org.redisson.api.stream.StreamReadArgs
import org.redisson.api.stream.StreamTrimArgs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class RedisOpsManager(
    redis: RedisRoomResources,
) : IOpsManager {
    private val opsStream = redis.getOpsStream()

    private val summaryBucket = redis.getSummaryBucket()

    companion object {
        private const val MESSAGE_KEY = "message"
    }

    override suspend fun append(clientId: Long, version: Long, ops: List<String>): SequencedOpsMessage {
        val message = OpsMessage(
            clientId = clientId,
            ops = ops,
        )

        val args = StreamAddArgs.entry(MESSAGE_KEY, message)

        val sequenceNumber = opsStream.addAsync(args)
            .toCompletableFuture()
            .await()

        return SequencedOpsMessage(
            clientId = clientId,
            sequenceNumber = sequenceNumber,
            ops = ops,
        )
    }

    override suspend fun getOpsSince(sequenceNumber: SequenceNumber): List<SequencedOpsMessage> {
        val args = StreamReadArgs.greaterThan(sequenceNumber)

        val messages = opsStream.readAsync(args)
            .toCompletableFuture()
            .await()

        return messages.map { (sequenceNumber, fields) ->
            val message = fields[MESSAGE_KEY]!!

            SequencedOpsMessage(
                clientId = message.clientId,
                sequenceNumber = sequenceNumber,
                ops = message.ops,
            )
        }
    }

    override suspend fun appendSummary(clientId: Int, sequenceNumber: SequenceNumber, summary: String) {
        val message = SummaryMessage(
            clientId = clientId,
            sequenceNumber = sequenceNumber,
            summary = summary,
        )

        summaryBucket.setAsync(message)
            .toCompletableFuture()
            .await()


        opsStream.trimAsync(StreamTrimArgs.minId(sequenceNumber.next()).noLimit())
            .toCompletableFuture()
            .await()
    }

    override suspend fun getSummary(): SummaryMessage {
        return summaryBucket.getAsync()
            .toCompletableFuture()
            .await()
    }

    private fun SequenceNumber.next() = StreamMessageId(id0, id1 + 1)

    override fun incoming(initialId: SequenceNumber) = flow<List<SequencedOpsMessage>> {
        var lastId = initialId

        while (true) {
            val ops = poll(lastId, 10.seconds)

            if (ops.isNotEmpty()) {
                lastId = ops.last().sequenceNumber

                emit(ops)
            }
        }
    }

    suspend fun poll(lastId: SequenceNumber, timeout: Duration): List<SequencedOpsMessage> {
        val args = StreamReadArgs.greaterThan(lastId)
            .timeout(timeout.toJavaDuration())

        val messages = opsStream.readAsync(args)
            .toCompletableFuture()
            .await()

        return messages.map { (sequenceNumber, fields) ->
            val message = fields[MESSAGE_KEY]!!

            SequencedOpsMessage(
                clientId = message.clientId,
                sequenceNumber = sequenceNumber,
                ops = message.ops,
            )
        }
    }
}