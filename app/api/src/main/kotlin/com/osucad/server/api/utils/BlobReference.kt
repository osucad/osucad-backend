package com.osucad.server.api.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.lang.UnsupportedOperationException

@Serializable(with = AssetUrlSerializer::class)
class BlobReference(val key: String) {
    fun asUrl(): String {
        return "/api/v1/blobs/$key"
    }
}

object AssetUrlSerializer : KSerializer<BlobReference> {
    override val descriptor = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): BlobReference {
        throw UnsupportedOperationException("Cannot deserialize BlobReference")
    }

    override fun serialize(encoder: Encoder, value: BlobReference) = encoder.encodeString("/api/v1/blobs/${value.key}")

}