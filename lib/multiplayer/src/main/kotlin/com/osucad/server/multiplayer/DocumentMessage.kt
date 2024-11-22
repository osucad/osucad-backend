package com.osucad.server.multiplayer

import kotlinx.serialization.Serializable

@Serializable
sealed interface DocumentMessage {
    val clientSequenceNumber: SequenceNumber
    val referenceSequenceNumber: SequenceNumber

    @Serializable
    data class ClientJoin(
        override val clientSequenceNumber: SequenceNumber,
        override val referenceSequenceNumber: SequenceNumber,
        val clientId: String,
    ) : DocumentMessage

    @Serializable
    data class ClientLeave(
        override val clientSequenceNumber: SequenceNumber,
        override val referenceSequenceNumber: SequenceNumber,
        val clientId: String,
    ) : DocumentMessage
}