package com.osucad.server.multiplayer.messages

import com.osucad.server.multiplayer.types.ClientInfo
import com.osucad.server.multiplayer.types.SequencedOpsMessage
import com.osucad.server.multiplayer.types.SummaryWithOps
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ServerMessage {

    @Serializable
    @SerialName("initial_state")
    data class InitialState(
        val clientId: Long,
        val document: SummaryWithOps,
        val connectedUsers: List<ClientInfo>,
    ) : ServerMessage


    @Serializable
    @SerialName("user_joined")
    data class UserJoined(
        val client: ClientInfo,
    ) : ServerMessage


    @Serializable
    @SerialName("user_left")
    data class UserLeft(
        val client: ClientInfo,
    ) : ServerMessage


    @Serializable
    @SerialName("ops_submitted")
    data class OpsSubmitted(
        val ops: List<SequencedOpsMessage>,
    ) : ServerMessage

}