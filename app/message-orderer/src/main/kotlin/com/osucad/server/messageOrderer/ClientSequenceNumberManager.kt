package com.osucad.server.messageOrderer

import com.osucad.server.multiplayer.SequenceNumber
import kotlinx.datetime.Instant

class ClientSequenceNumberManager {
    private val clientNodeMap = mutableMapOf<String, ClientSequenceNumber>()

    fun upsertClient(
        clientId: String,
        clientSequenceNumber: SequenceNumber,
        referenceSequenceNumber: SequenceNumber,
        timestamp: Instant,
    ): Boolean {
        val client = clientNodeMap[clientId]

        if (client != null) {
            client.clientSequenceNumber = clientSequenceNumber
            client.referenceSequenceNumber = referenceSequenceNumber
            client.lastUpdate = timestamp

            return false
        }

        val newClient = ClientSequenceNumber(
            clientId = clientId,
            clientSequenceNumber = clientSequenceNumber,
            referenceSequenceNumber = referenceSequenceNumber,
            lastUpdate = timestamp,
        )

        clientNodeMap[clientId] = newClient

        return true
    }

    fun removeClient(clientId: String): Boolean {
        return clientNodeMap.remove(clientId) != null
    }

    fun get(clientId: String) = clientNodeMap[clientId]


}