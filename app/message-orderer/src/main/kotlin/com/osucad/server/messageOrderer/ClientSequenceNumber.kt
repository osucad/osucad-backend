package com.osucad.server.messageOrderer

import kotlinx.datetime.Instant

data class ClientSequenceNumber(
    var clientId: String,
    /**
     * The local sequence number of the client
     */
    var clientSequenceNumber: Long,
    /**
     * The sequence number of the latest summary the client has
     */
    var referenceSequenceNumber: Long,
    var lastUpdate: Instant,
)