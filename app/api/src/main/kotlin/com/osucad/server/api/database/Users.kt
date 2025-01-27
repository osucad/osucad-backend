package com.osucad.server.api.database

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

enum class UserRole {
    User,
    Admin
}

object Users : IdTable<Int>() {
    override val id = integer("id").entityId()

    override val primaryKey = PrimaryKey(id)

    val username = varchar("username", 30)
    val avatarUrl = varchar("avatar_url", 255)

    val registeredAt = timestamp("registered_at").defaultExpression(CurrentTimestamp)

    val restricted = bool("restricted").default(false)

    val role = enumerationByName<UserRole>("role", 32).default(UserRole.User)
}