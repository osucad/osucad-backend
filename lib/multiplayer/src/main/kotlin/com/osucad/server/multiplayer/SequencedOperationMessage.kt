package com.osucad.server.multiplayer

import kotlinx.serialization.Serializable

@Serializable
data class SequencedOperationMessage(
    val documentId: String,
    val operation: SequencedDocumentMessage,
)