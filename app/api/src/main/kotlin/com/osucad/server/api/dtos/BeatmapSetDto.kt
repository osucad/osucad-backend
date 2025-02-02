package com.osucad.server.api.dtos

import com.osucad.server.api.domain.BeatmapSet
import kotlinx.serialization.Serializable

@Serializable
class BeatmapSetDto(
    val id: String,
    val beatmaps: List<BeatmapDto>,
    val title: String,
    val artist: String,
    val creator: String,
    val audioUrl: String,
    val covers: BeatmapCoversDto?,
)

fun BeatmapSet.toDto(): BeatmapSetDto {
    check(beatmaps.isNotEmpty())

    val referenceBeatmap = beatmaps[0]

    return BeatmapSetDto(
        id = id,
        beatmaps = beatmaps.map { it.toDto() },
        title = referenceBeatmap.title,
        artist = referenceBeatmap.artist,
        creator = referenceBeatmap.creator,
        audioUrl = referenceBeatmap.audioFile.asUrl(),
        covers = beatmaps
            .firstOrNull { it.backgroundFile != null }
            ?.let(BeatmapCoversDto::fromBeatmap)
    )
}