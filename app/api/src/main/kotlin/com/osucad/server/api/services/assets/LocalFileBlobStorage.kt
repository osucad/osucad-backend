package com.osucad.server.api.services.assets

import io.ktor.server.application.ApplicationCall
import io.ktor.server.http.content.LocalPathContent
import io.ktor.server.response.respond
import org.koin.core.annotation.Single
import java.io.InputStream
import java.nio.file.Path
import java.security.DigestInputStream
import java.security.MessageDigest
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.moveTo
import kotlin.io.path.outputStream

@Single
class LocalFileBlobStorage(
    private val rootDirectory: Path = Path.of("build/.blobs")
) : IBlobStorage {
    init {
        rootDirectory.createDirectories()
    }

    private fun getPath(key: String) = rootDirectory.resolve(key)

    override suspend fun put(inputStream: InputStream): IBlob {
        val tmpFile = createTempFile()

        return try {
            val digest = MessageDigest.getInstance("SHA-1")

            val messageDigest = DigestInputStream(inputStream, digest).use { digestInputStream ->
                tmpFile.outputStream().use { outputStream ->
                    digestInputStream.copyTo(outputStream)
                }

                digestInputStream.messageDigest
            }

            val key = messageDigest.digest().joinToString("") { "%02x".format(it) }

            val path = getPath(key)

            if (path.exists())
                return LocalFileBlob(key, path)

            tmpFile.moveTo(getPath(key))

            LocalFileBlob(key, path)
        } finally {
            tmpFile.deleteIfExists()
        }
    }

    override suspend fun get(key: String): IBlob? {
        val path = getPath(key)

        if (!path.exists())
            return null

        return LocalFileBlob(key, path)
    }

    class LocalFileBlob(override val key: String, private val path: Path) : IBlob {
        override fun inputStream(): InputStream {
            return path.inputStream()
        }

        override suspend fun writeResponse(call: ApplicationCall) {
            call.respond(LocalPathContent(path))
        }
    }
}