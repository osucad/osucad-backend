package com.osucad.server.api.repositories

import com.osucad.server.api.database.Users
import com.osucad.server.api.domain.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single


fun User.Companion.fromRow(row: ResultRow) = with(Users) {
    User(
        id = row[id].value,
        username = row[username],
        avatarUrl = row[avatarUrl],
        registeredAt = row[registeredAt],
        restricted = row[restricted],
        role = row[role]
    )
}


@Single
class UserRepository : IUserRepository {
    fun findById(id: Int): User? {
        val row = Users.selectAll().where { Users.id eq id }.singleOrNull()

        return row?.let(User::fromRow)
    }

    class CreateUserOptions(
        val id: Int,
        val username: String,
        val avatarUrl: String,
    )

    fun create(user: CreateUserOptions): User {
        Users.insert {
            it[id] = user.id
            it[username] = user.username
            it[avatarUrl] = user.avatarUrl
        }

        return findById(user.id)!!
    }

    fun update(user: User) {
        Users.update({ Users.id eq user.id }) {
            it[username] = user.username
            it[avatarUrl] = user.avatarUrl
            it[restricted] = user.restricted
            it[role] = user.role
        }
    }
}