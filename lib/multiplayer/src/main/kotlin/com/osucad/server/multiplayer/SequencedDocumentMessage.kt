package com.osucad.server.multiplayer

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class SequencedDocumentMessage(
    val clientId: String? = null,
    val sequenceNumber: SequenceNumber,
    val clientSequenceNumber: SequenceNumber,
    val referenceSequenceNumber: SequenceNumber,
    val contents: DocumentMessage, // TODO: we shouldn't reuse the document message
    val timestamp: Instant,
)