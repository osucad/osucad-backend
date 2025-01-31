package com.osucad.server.api.config

import com.osucad.server.api.routes.beatmapSetRoutes
import com.osucad.server.api.routes.blobRoutes
import com.osucad.server.api.routes.roomRoutes
import com.osucad.server.api.routes.userRoutes
import io.ktor.server.application.Application
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        route("/api/v1") {
            route("/users", Route::userRoutes)
            route("/beatmapsets", Route::beatmapSetRoutes)
            route("/blobs", Route::blobRoutes)
            route("/rooms", Route::roomRoutes)
        }
    }
}