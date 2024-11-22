package com.osucad.server.messageOrderer

import com.osucad.server.multiplayer.SequenceNumber
import kotlinx.datetime.Instant

data class ClientSequenceNumber(
    val clientId: String,
    /**
     * The local sequence number of the client
     */
    var clientSequenceNumber: SequenceNumber,
    /**
     * The sequence number of the latest summary the client has
     */
    var referenceSequenceNumber: SequenceNumber,
    var lastUpdate: Instant,
)