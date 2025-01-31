package com.osucad.server.multiplayer.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.redisson.api.StreamMessageId

typealias SequenceNumber = @Serializable(with = SequenceNumberSerializer::class) StreamMessageId

object SequenceNumberSerializer : KSerializer<SequenceNumber> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SequenceNumber", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SequenceNumber) {
        return encoder.encodeString("${value.id0}-${value.id1}")
    }

    override fun deserialize(decoder: Decoder): SequenceNumber {
        val string = decoder.decodeString()

        val (id0, id1) = string.split("-").map { it.toLong() }

        return SequenceNumber(id0, id1)
    }
}