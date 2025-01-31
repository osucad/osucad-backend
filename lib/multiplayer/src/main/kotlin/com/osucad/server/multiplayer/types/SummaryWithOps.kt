package com.osucad.server.multiplayer.types

import kotlinx.serialization.Serializable

@Serializable
data class SummaryWithOps(
    val summary: SummaryMessage,
    val ops: List<SequencedOpsMessage>,
)