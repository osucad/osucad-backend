package com.osucad.server.multiplayer.local

import com.osucad.server.multiplayer.IOpsManager
import com.osucad.server.multiplayer.types.AssetInfo
import com.osucad.server.multiplayer.types.SequenceNumber
import com.osucad.server.multiplayer.types.SequencedOpsMessage
import com.osucad.server.multiplayer.types.SummaryMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.map
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class LocalOpsManager : IOpsManager {
    private var summary: SummaryMessage? = null

    private val ops = Collections.synchronizedList(mutableListOf<SequencedOpsMessage>())

    private val opsFlow = MutableSharedFlow<SequencedOpsMessage>(extraBufferCapacity = 100)

    private val sequenceNumber = AtomicLong(0)

    override suspend fun initializeFromSummary(summary: SummaryMessage) {
        this.summary = summary
    }

    override suspend fun append(
        clientId: Long,
        version: Long,
        ops: List<String>
    ): SequencedOpsMessage {
        val sequenceNumber = SequenceNumber(sequenceNumber.incrementAndGet())

        val message = SequencedOpsMessage(
            clientId = clientId,
            sequenceNumber = sequenceNumber,
            version = version,
            ops = ops,
        )

        opsFlow.emit(message)

        return message
    }

    override suspend fun getOpsSince(sequenceNumber: SequenceNumber): List<SequencedOpsMessage> {
        return ops.filter { it.sequenceNumber.id0 >= sequenceNumber.id0 }
    }

    override suspend fun appendSummary(
        clientId: Int,
        sequenceNumber: SequenceNumber,
        summary: String,
        assets: List<AssetInfo>,
    ) {
        this.summary = SummaryMessage(
            clientId = clientId,
            sequenceNumber = sequenceNumber,
            summary = summary,
            assets = assets,
        )

        ops.removeIf { it.sequenceNumber.id0 < sequenceNumber.id0 }
    }

    override suspend fun getSummary(): SummaryMessage {
        return summary!!
    }

    override fun incoming(initialId: SequenceNumber): Flow<List<SequencedOpsMessage>> {
        return opsFlow
            .dropWhile { it.sequenceNumber.id0 < initialId.id0 }
            .map { listOf(it) }
    }
}