package com.osucad.server.api.routes

import com.osucad.server.api.dtos.UserDto
import com.osucad.server.api.exceptions.InternalServerErrorException
import com.osucad.server.api.utils.getUser
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.userRoutes() {
    authenticate {
        route("/me", Route::ownUserRoute)
    }
}

private fun Route.ownUserRoute() {
    get {
        val user = call.getUser() ?: throw InternalServerErrorException("User not found")

        call.respond(UserDto.map(user))
    }
}