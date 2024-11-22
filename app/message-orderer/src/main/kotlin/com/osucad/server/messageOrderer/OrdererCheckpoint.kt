package com.osucad.server.messageOrderer

class OrdererCheckpoint(
    val sequenceNumber: Long,
    val clients: List<ClientSequenceNumber>,
)