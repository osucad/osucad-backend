package com.osucad.server.api.routes

import com.osucad.server.api.domain.BeatmapSet
import com.osucad.server.api.dtos.toDto
import com.osucad.server.api.exceptions.BadRequestException
import com.osucad.server.api.services.beatmaps.BeatmapSetImportService
import com.osucad.server.api.services.beatmaps.BeatmapSetService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import org.koin.ktor.ext.inject
import java.lang.IllegalStateException
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.fileSize
import kotlin.io.path.writeBytes

fun Route.beatmapSetRoutes() {
    authenticate {
        route("/", Route::findAllRoute)
        route("/import/osz", Route::importOszRoute)
    }
}

private fun Route.findAllRoute() {
    val service by inject<BeatmapSetService>()

    get {
        call.respond(service.findAll().map(BeatmapSet::toDto))
    }
}

private fun Route.importOszRoute() {
    val importService by inject<BeatmapSetImportService>()
    val beatmapSetService by inject<BeatmapSetService>()

    suspend fun handleFilePart(part: PartData.FileItem): BeatmapSet {
        val tmpFile = createTempFile(suffix = ".zip")
        try {
            val data = part.provider()
                .readRemaining()
                .readByteArray()

            tmpFile.writeBytes(data)

            val id = importService.import(tmpFile)

            return beatmapSetService.findById(id)
                ?: throw IllegalStateException("No beatmapset created")
        } finally {
            tmpFile.deleteIfExists()
        }
    }

    post {

        val multiPartData = call.receiveMultipart()

        when (val part = multiPartData.readPart()) {
            is PartData.FileItem -> {
                val beatmapSet = handleFilePart(part)

                call.respond(beatmapSet.toDto())
            }

            else -> throw BadRequestException()
        }

        // We only want one file per request
        if (multiPartData.readPart() != null)
            throw BadRequestException()

        call.respond(HttpStatusCode.OK, "OK")
    }
}