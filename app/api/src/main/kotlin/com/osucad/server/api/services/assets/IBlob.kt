package com.osucad.server.api.services.assets

import io.ktor.server.application.ApplicationCall
import java.io.InputStream

interface IBlob {
    val key: String
    fun inputStream(): InputStream

    suspend fun writeResponse(call: ApplicationCall)
}