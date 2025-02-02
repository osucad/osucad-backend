package com.osucad.server.multiplayer.redis

import com.osucad.server.multiplayer.types.UserInfo

class RedisClientInfo(
    val clientId: Long,
    val user: UserInfo,
    val color: String,
    var presence: Map<String, String>
)