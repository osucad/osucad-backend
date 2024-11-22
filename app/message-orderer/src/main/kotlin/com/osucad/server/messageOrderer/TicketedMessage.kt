package com.osucad.server.messageOrderer

import com.osucad.server.multiplayer.RawOperationMessage
import com.osucad.server.multiplayer.SequenceNumber
import com.osucad.server.multiplayer.SequencedDocumentMessage
import kotlinx.datetime.Instant

class TicketedMessage(
    val ticketType: TicketType,
    val message: SequencedDocumentMessage,
    val msn: SequenceNumber,
    val timestamp: Instant,
)

enum class TicketType {
    Sequenced,
    Signal,
}