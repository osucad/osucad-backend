package com.osucad.server.messageOrderer

import com.osucad.server.multiplayer.DocumentMessage
import com.osucad.server.multiplayer.RawOperationMessage
import com.osucad.server.multiplayer.SequenceNumber
import com.osucad.server.multiplayer.SequencedDocumentMessage
import com.osucad.server.multiplayer.SequencedOperationMessage
import kotlinx.coroutines.channels.SendChannel

class MessageOrderer(
    private val documentId: String,
    private val deltasProducer: SendChannel<SequencedOperationMessage>,
    private val clientSeqManager: ClientSequenceNumberManager,
) {
    private var sequenceNumber = 0L

    suspend fun handle(message: RawOperationMessage) =
        handle(QueuedMessage(listOf(message)))

    suspend fun handle(rawMessages: QueuedMessage) {
        for (message in rawMessages.messages) {
            val ticketedMessage = ticket(message) ?: continue

            // TODO: handle other message types

            val outgoingMessage = SequencedOperationMessage(
                documentId = documentId,
                operation = ticketedMessage
            )

            // TODO: batching
            deltasProducer.send(outgoingMessage)
        }
    }

    private fun ticket(message: RawOperationMessage): SequencedDocumentMessage? {
        val operation = message.operation

        val clientId = message.clientId

        when (operation) {
            is DocumentMessage.ClientLeave -> {
                if (!clientSeqManager.removeClient(operation.clientId))
                    return null
            }

            is DocumentMessage.ClientJoin -> {
                val isNewClient = clientSeqManager.upsertClient(
                    clientId = operation.clientId,
                    clientSequenceNumber = operation.clientSequenceNumber,
                    referenceSequenceNumber = operation.referenceSequenceNumber,
                    timestamp = message.timestamp,
                )

                if (!isNewClient)
                    return null
            }
        }

        val sequenceNumber = revSequenceNumber()

        if (clientId != null && operation !is DocumentMessage.ClientLeave) {
            clientSeqManager.upsertClient(
                clientId = clientId,
                clientSequenceNumber = message.operation.clientSequenceNumber,
                referenceSequenceNumber = message.operation.referenceSequenceNumber,
                timestamp = message.timestamp,
            )
        }

        return SequencedDocumentMessage(
            clientId = clientId,
            sequenceNumber = sequenceNumber,
            clientSequenceNumber = operation.clientSequenceNumber,
            referenceSequenceNumber = operation.referenceSequenceNumber,
            contents = operation,
            timestamp = message.timestamp
        )
    }

    private fun revSequenceNumber(): SequenceNumber {
        return ++sequenceNumber
    }

}