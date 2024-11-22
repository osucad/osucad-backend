package com.osucad.server.messageOrderer

import com.osucad.server.multiplayer.*
import kotlinx.coroutines.channels.SendChannel

class MessageOrderer(
    private val documentId: String,
    private val lastCheckpoint: OrdererCheckpoint,
    private val deltasProducer: SendChannel<SequencedOperationMessage>, // TODO
    private val signalProducer: SendChannel<Any>, // TODO
    private val clientManager: ClientSequenceNumberManager,
) {

    private var sequenceNumber = lastCheckpoint.sequenceNumber

    private var minimumSequenceNumber: Long = -1L

    init {
        for (client in lastCheckpoint.clients) {
            clientManager.upsertClient(
                clientId = client.clientId,
                clientSequenceNumber = client.clientSequenceNumber,
                referenceSequenceNumber = client.referenceSequenceNumber,
                timestamp = client.lastUpdate
            )
        }

        updateMinimumSequenceNumber(sequenceNumber)
    }


    suspend fun handle(rawMessages: QueuedMessage) {
        var sequenceMessageCount = 0

        for (message in rawMessages.messages) {
            val ticketedMessage = ticket(message) ?: continue

            when (ticketedMessage.ticketType) {
                TicketType.Sequenced -> {
                    val outgoingMessage = SequencedOperationMessage(
                        documentId = documentId,
                        operation = ticketedMessage.message
                    )

                    // TODO: batching
                    deltasProducer.send(outgoingMessage)
                }

                else -> {}
            }
        }
    }

    private suspend fun ticket(message: RawOperationMessage): TicketedMessage? {
        val operation = message.operation

        val clientId = message.clientId

        when (operation) {
            is DocumentMessage.ClientLeave -> {
                if (!clientManager.removeClient(operation.clientId))
                    return null
            }

            is DocumentMessage.ClientJoin -> {
                val isNewClient = clientManager.upsertClient(
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

        if (clientId != null) {
            clientManager.upsertClient(
                clientId = clientId,
                clientSequenceNumber = message.operation.clientSequenceNumber,
                referenceSequenceNumber = message.operation.referenceSequenceNumber,
                timestamp = message.timestamp,
            )
        }

        updateMinimumSequenceNumber(sequenceNumber)

        return TicketedMessage(
            ticketType = TicketType.Sequenced,
            message = SequencedDocumentMessage(
                clientId = clientId,
                sequenceNumber = sequenceNumber,
                clientSequenceNumber = operation.clientSequenceNumber,
                referenceSequenceNumber = operation.referenceSequenceNumber,
                contents = operation,
                timestamp = message.timestamp
            ),
            msn = minimumSequenceNumber,
            timestamp = message.timestamp,
        )
    }

    private fun updateMinimumSequenceNumber(sequenceNumber: SequenceNumber) {
        minimumSequenceNumber = when (val msn = clientManager.minimumSequenceNumber()) {
            null -> sequenceNumber
            else -> msn
        }
    }

    private fun revSequenceNumber(): Long {
        return ++sequenceNumber
    }

}