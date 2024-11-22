package com.osucad.server.multiplayer

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class RawOperationMessage(
    val operation: DocumentMessage,
    var timestamp: Instant,
    val clientId: String?
)