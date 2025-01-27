package com.osucad.server.api.services.user

import com.osucad.server.api.database.Isolation
import com.osucad.server.api.database.dbQuery
import com.osucad.server.api.domain.User
import com.osucad.server.api.repositories.UserRepository
import org.koin.core.annotation.Single
import org.slf4j.LoggerFactory

@Single
class UserService(
    private val userRepository: UserRepository
) : IUserService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun findById(id: Int): User? = dbQuery { userRepository.findById(id) }

    override suspend fun updateOrCreate(
        id: Int,
        username: String,
        avatarUrl: String,
    ): User = dbQuery(isolation = Isolation.Serializable) {
        when (val user = userRepository.findById(id)) {
            null -> {
                val newUser = userRepository.create(
                    UserRepository.CreateUserOptions(
                        id = id,
                        username = username,
                        avatarUrl = avatarUrl,
                    )
                )

                logger.info("Created new user (id=$id, username=$username)")

                newUser

            }

            else -> {
                var hasChanges = user.username != username || user.avatarUrl != avatarUrl

                if (hasChanges) {
                    user.username = username
                    user.avatarUrl = avatarUrl

                    userRepository.update(user)
                }

                user
            }
        }
    }
}