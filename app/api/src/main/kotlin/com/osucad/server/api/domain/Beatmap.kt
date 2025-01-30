package com.osucad.server.api.domain

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
    val audioUrl: String,
    val backgroundUrl: String?,
)