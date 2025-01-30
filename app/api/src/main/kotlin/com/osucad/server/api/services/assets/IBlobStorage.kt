package com.osucad.server.api.services.assets

import java.io.InputStream

interface IBlobStorage {
    suspend fun put(inputStream: InputStream): IBlob

    suspend fun get(key: String): IBlob?
}