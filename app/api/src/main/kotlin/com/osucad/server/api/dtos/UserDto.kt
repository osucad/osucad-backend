package com.osucad.server.api.dtos

import com.osucad.server.api.domain.User
import kotlinx.serialization.Serializable
import tech.mappie.api.ObjectMappie

@Serializable
class UserDto(
    val id: Int,
    val username: String,
    val avatarUrl: String,
) {
    companion object : ObjectMappie<User, UserDto>()
}