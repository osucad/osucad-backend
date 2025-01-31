package com.osucad.server.multiplayer.types

import kotlinx.serialization.Serializable

@Serializable
class UserInfo(
    val id: Int,
    val username: String,
)