package com.osucad.server.multiplayer.types

import kotlinx.serialization.Serializable

@Serializable
data class ClientInfo(
    val clientId: Long,
    val user: UserInfo,
    val color: String,
)