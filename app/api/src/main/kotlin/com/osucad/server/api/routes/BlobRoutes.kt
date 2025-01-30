package com.osucad.server.api.routes

import com.osucad.server.api.exceptions.NotFoundException
import com.osucad.server.api.services.assets.IBlobStorage
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject

fun Route.blobRoutes() {
    val blobStorage by inject<IBlobStorage>()

    get("/{key}") {
        val key = call.parameters["key"]!!

        val blob = blobStorage.get(key)
            ?: throw NotFoundException()

        blob.writeResponse(call)
    }
}