package com.osucad.server.multiplayer

import kotlinx.serialization.Serializable

@Serializable
sealed interface DocumentMessage {
    val clientSequenceNumber: SequenceNumber
    val referenceSequenceNumber: SequenceNumber

    @Serializable
    class ClientJoin(
        override val clientSequenceNumber: SequenceNumber,
        override val referenceSequenceNumber: SequenceNumber,
        val clientId: String,
    ) : DocumentMessage

    @Serializable
    class ClientLeave(
        override val clientSequenceNumber: SequenceNumber,
        override val referenceSequenceNumber: SequenceNumber,
        val clientId: String,
    ) : DocumentMessage
}