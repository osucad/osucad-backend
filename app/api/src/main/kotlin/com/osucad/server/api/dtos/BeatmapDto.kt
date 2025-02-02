package com.osucad.server.api.dtos

import com.osucad.server.api.domain.Beatmap
import kotlinx.serialization.Serializable

@Serializable
class BeatmapDto(
    val id: String,
    val difficultyName: String,
    val artist: String,
    val artistUnicode: String,
    val title: String,
    val titleUnicode: String,
    val creator: String,
    val audioUrl: String,
    val covers: BeatmapCoversDto?,
) {

}

fun Beatmap.toDto() = BeatmapDto(
    id = id,
    difficultyName = difficultyName,
    artist = artist,
    artistUnicode = artistUnicode,
    title = title,
    titleUnicode = titleUnicode,
    creator = creator,
    audioUrl = "api/v1/blobs/${audioFile.key}",
    covers = BeatmapCoversDto.fromBeatmap(this)
)