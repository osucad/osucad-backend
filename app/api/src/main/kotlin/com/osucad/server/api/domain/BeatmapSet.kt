package com.osucad.server.api.domain

import kotlinx.serialization.Serializable

@Serializable
class BeatmapSet(
    val id: String,
    val beatmaps: List<Beatmap>,
    val title: String,
    val artist: String,
)