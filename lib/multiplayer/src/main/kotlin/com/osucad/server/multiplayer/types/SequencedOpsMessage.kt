package com.osucad.server.multiplayer.types

import kotlinx.serialization.Serializable

@Serializable
data class SequencedOpsMessage(
    val clientId: Long,
    val sequenceNumber: SequenceNumber,
    val ops: List<String>,
)