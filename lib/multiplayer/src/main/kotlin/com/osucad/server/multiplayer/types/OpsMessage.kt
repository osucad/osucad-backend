package com.osucad.server.multiplayer.types

data class OpsMessage(
    val clientId: Long,
    val ops: List<String>,
)