package com.osucad.server.multiplayer.types

import kotlinx.serialization.Serializable

@Serializable
class SummaryMessage(
    val clientId: Int,
    val sequenceNumber: SequenceNumber,
    val summary: String,
)