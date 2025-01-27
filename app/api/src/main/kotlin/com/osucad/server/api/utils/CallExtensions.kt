package com.osucad.server.api.utils

import com.osucad.server.api.config.UserSession
import com.osucad.server.api.domain.User
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import io.ktor.util.*

fun ApplicationCall.getUserId(): Int? = sessions.get<UserSession>()?.userId

val UserAttributeKey = AttributeKey<User>("osucad:user")

fun ApplicationCall.getUser(): User? = attributes[UserAttributeKey]

fun ApplicationCall.setUser(user: User) {
    attributes.put(UserAttributeKey, user)
}
