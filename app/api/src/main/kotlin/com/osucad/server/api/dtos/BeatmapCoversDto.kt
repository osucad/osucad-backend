package com.osucad.server.api.dtos

import com.osucad.server.api.domain.Beatmap
import kotlinx.serialization.Serializable

@Serializable
class BeatmapCoversDto(
    val large: String,
    val list: String,
) {
    companion object {
        fun fromBeatmap(beatmap: Beatmap): BeatmapCoversDto? {
            if (beatmap.backgroundFile == null)
                return null

            return BeatmapCoversDto(
                large = beatmap.backgroundFile.asUrl(),
                list = beatmap.backgroundFile.asUrl(),
            )
        }
    }
}