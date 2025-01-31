package com.osucad.server.multiplayer.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ClientMessage {
    @Serializable
    @SerialName("submit_ops")
    class SubmitOps(
        val version: Long,
        val ops: List<String>,
    ) : ClientMessage
}