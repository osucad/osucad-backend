package com.osucad.server.api.routes

import com.osucad.server.api.exceptions.BadRequestException
import com.osucad.server.api.services.beatmaps.BeatmapSetImportService
import com.osucad.server.api.services.beatmaps.BeatmapSetService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import org.koin.ktor.ext.inject
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.fileSize
import kotlin.io.path.writeBytes

fun Route.beatmapSetRoutes() {
    route("/") {
        findAllRoute()
    }
    route("/import/osz") {
        importOszRoute()
    }
}

private fun Route.findAllRoute() {
    val service by inject<BeatmapSetService>()

    get {
        call.respond(service.findAll())
    }
}

private fun Route.importOszRoute() {
    val importService by inject<BeatmapSetImportService>()

    suspend fun handleFilePart(part: PartData.FileItem) {
        val tmpFile = createTempFile(suffix = ".zip")
        try {
            val data =part.provider()
                .readRemaining()
                .readByteArray()

            tmpFile.writeBytes(data)

            println(tmpFile.fileSize())

            importService.import(tmpFile)
        } finally {
            tmpFile.deleteIfExists()
        }
    }

    post {

        val multiPartData = call.receiveMultipart()

        when (val part = multiPartData.readPart()) {
            is PartData.FileItem -> {
                handleFilePart(part)
            }

            else -> throw BadRequestException()
        }

        // We only want one file per request
        if (multiPartData.readPart() != null)
            throw BadRequestException()

        call.respond(HttpStatusCode.OK, "OK")

    }
}