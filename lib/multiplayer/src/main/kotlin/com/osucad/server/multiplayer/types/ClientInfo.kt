package com.osucad.server.multiplayer.types

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ClientInfo(
    val clientId: Long,
    val user: UserInfo,
    val color: String,
    var presence: Map<String, JsonElement>,
)