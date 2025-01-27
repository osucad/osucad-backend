package com.osucad.server.api.domain

import com.osucad.server.api.database.UserRole
import kotlinx.datetime.Instant

open class User(
    val id: Int,
    var username: String,
    var avatarUrl: String,
    var registeredAt: Instant,
    var restricted: Boolean,
    var role: UserRole,
) {
    companion object

    val isAdmin get() = role == UserRole.Admin
}