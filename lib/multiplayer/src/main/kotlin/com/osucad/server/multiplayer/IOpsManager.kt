package com.osucad.server.multiplayer

import com.osucad.server.multiplayer.types.AssetInfo
import com.osucad.server.multiplayer.types.SequenceNumber
import com.osucad.server.multiplayer.types.SequencedOpsMessage
import com.osucad.server.multiplayer.types.SummaryMessage
import kotlinx.coroutines.flow.Flow

interface IOpsManager {
    suspend fun initializeFromSummary(summary: SummaryMessage)

    suspend fun append(clientId: Long, version: Long, ops: List<String>): SequencedOpsMessage

    suspend fun getOpsSince(sequenceNumber: SequenceNumber): List<SequencedOpsMessage>

    suspend fun appendSummary(clientId: Int, sequenceNumber: SequenceNumber, summary: String, assets: List<AssetInfo>,)

    suspend fun getSummary(): SummaryMessage

    fun incoming(initialId: SequenceNumber): Flow<List<SequencedOpsMessage>>
}