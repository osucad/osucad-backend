package com.osucad.server.multiplayer.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
sealed interface ClientMessage {
    @Serializable
    @SerialName("submit_ops")
    class SubmitOps(
        val version: Long,
        val ops: List<String>,
    ) : ClientMessage

    @Serializable
    @SerialName("update_presence")
    class UpdatePresence(
        val key: String,
        val value: JsonElement,
    ) : ClientMessage
}