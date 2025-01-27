package com.osucad.server.api.services.user

import com.osucad.server.api.domain.User


interface IUserService {
    suspend fun findById(id: Int): User?

    suspend fun updateOrCreate(
        id: Int,
        username: String,
        avatarUrl: String,
    ): User
}