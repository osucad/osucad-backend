package com.osucad.server.api.routes

import com.osucad.server.api.exceptions.BadRequestException
import com.osucad.server.api.services.multiplayer.RoomService
import com.osucad.server.api.utils.getUser
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.getKoin
import java.util.*

fun Route.roomRoutes() {
    authenticate {
        route("/{id}/token", Route::getRoomTokenRoute)
    }
}

fun Route.getRoomTokenRoute() {
    val service = getKoin().get<RoomService>()


    @Serializable
    class TokenResponse(val accessToken: String)

    post {
        val id = call.pathParameters["id"]?.let(UUID::fromString)
            ?: throw BadRequestException()

        val token = service.createToken(id, call.getUser()!!)

        call.respond(TokenResponse(token))
    }
}