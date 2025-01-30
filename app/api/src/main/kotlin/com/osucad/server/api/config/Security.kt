package com.osucad.server.api.config

import com.osucad.server.api.exceptions.UnauthorizedException
import com.osucad.server.api.services.user.IUserService
import com.osucad.server.api.utils.UserAttributeKey
import com.osucad.server.api.utils.getUser
import com.osucad.server.api.utils.getValue
import com.osucad.server.api.utils.trimTrailingSlash
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.get
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.time.Duration.Companion.days

class OsuOAuthConfig(config: ApplicationConfig) {
    val clientId by config
    val clientSecret by config
    val callbackUrl by config
}

class SessionConfig(config: ApplicationConfig) {
    val storageDirectory = File(config.property("storageDirectory").getString())
}


@Serializable
class UserSession(val userId: Int)

fun Application.configureSecurity() {
    val isProduction = !developmentMode

    val sessionConfig = get<SessionConfig>()
    val osuOAuthConfig = get<OsuOAuthConfig>()
    val serverConfig = get<ApiServerConfig>()

    val userService = get<IUserService>()

    val sessionDuration = 30.days

    install(Sessions) {
        cookie<UserSession>("user_session", directorySessionStorage(sessionConfig.storageDirectory)) {
            cookie.path = "/"
            cookie.httpOnly = true
            cookie.secure = isProduction
            cookie.maxAge = sessionDuration
            cookie.extensions["SameSite"] = "Lax"
        }
    }

    val redirects = mutableMapOf<String, String>()

    install(Authentication) {
        session<UserSession> {
            validate { session ->
                val user = userService.findById(session.userId)

                if (user != null) {
                    attributes.put(UserAttributeKey, user)

                    session
                } else {
                    null
                }
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized, HttpStatusCode.Unauthorized.description)
            }
        }

        oauth("oauth-osu") {
            urlProvider = { osuOAuthConfig.callbackUrl }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "osu",
                    authorizeUrl = "https://osu.ppy.sh/oauth/authorize",
                    accessTokenUrl = "https://osu.ppy.sh/oauth/token",
                    requestMethod = HttpMethod.Post,
                    clientId = osuOAuthConfig.clientId,
                    clientSecret = osuOAuthConfig.clientSecret,
                    defaultScopes = listOf("public", "identify"),
                    onStateCreated = { call, state ->
                        call.request.queryParameters["redirectUrl"]?.let {
                            redirects[state] = it
                        }
                    }
                )
            }
            client = applicationHttpClient
        }
    }

    routing {
        authenticate("oauth-osu") {
            route("/api/v1/auth/osu/") {
                val logger = LoggerFactory.getLogger("oauth-osu")

                get("/login") {
                    /* Redirects to 'authorizeUrl' automatically */
                }

                get("/callback") {
                    val currentPrincipal: OAuthAccessTokenResponse.OAuth2? = call.principal()

                    if (currentPrincipal == null)
                        throw UnauthorizedException()

                    val (id, username, avatarUrl) = getUserDetails(currentPrincipal.accessToken)

                    val user = userService.updateOrCreate(
                        id = id,
                        username = username,
                        avatarUrl = avatarUrl,
                    )

                    logger.info("User logged in (id=$id, username=$username)")

                    call.sessions.set(UserSession(user.id))

                    currentPrincipal.state?.let { state ->
                        redirects.remove(state)?.let { redirectUrl ->
                            call.respondRedirect(redirectUrl)
                            return@get
                        }
                    }

                    call.respondRedirect(serverConfig.clientUrl.trimTrailingSlash() + "/")
                }
            }
        }

        get("/api/v1/auth/logout") {
            val user = call.getUser()
            if (user != null) {
                log.info("User ${user.username} (${user.id}) logged out")
            }

            call.sessions.clear<UserSession>()
            call.respond(status = HttpStatusCode.OK, Unit)
        }
    }
}


@Serializable
data class UserDetails(
    val id: Int,
    val username: String,
    @SerialName("avatar_url")
    val avatarUrl: String,
)

suspend fun getUserDetails(accessToken: String): UserDetails {
    val response = applicationHttpClient.get("https://osu.ppy.sh/api/v2/me") {
        header("Authorization", "Bearer $accessToken")
    }


    return response.body()
}