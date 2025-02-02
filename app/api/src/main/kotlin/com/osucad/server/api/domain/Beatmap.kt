package com.osucad.server.api.domain

import com.osucad.server.api.utils.BlobReference
import kotlinx.serialization.Serializable

@Serializable
class Beatmap(
    val id: String,
    val difficultyName: String,
    val artist: String,
    val artistUnicode: String,
    val title: String,
    val titleUnicode: String,
    val creator: String,
    val audioFile: BlobReference,
    val backgroundFile: BlobReference?,
)