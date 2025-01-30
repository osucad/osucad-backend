package com.osucad.server.api.services.beatmaps

import com.osucad.server.api.database.BeatmapSets
import com.osucad.server.api.database.Beatmaps
import com.osucad.server.api.database.dbQuery
import com.osucad.server.api.domain.Beatmap
import com.osucad.server.api.domain.BeatmapSet
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single

@Single
class BeatmapSetService {
    suspend fun findAll(): List<BeatmapSet> = dbQuery {
        BeatmapSets.leftJoin(Beatmaps).selectAll()
            .where { Beatmaps.deleted eq false  }
            .toList()
            .groupBy { it[BeatmapSets.id] }
            .filter { (id, rows) -> rows.isNotEmpty() }
            .map { (id, rows) ->
                val metadataRow = rows.first()

                BeatmapSet(
                    id = id.value.toString(),
                    title = metadataRow[Beatmaps.title],
                    artist = metadataRow[Beatmaps.artist],
                    beatmaps = rows.map {
                        Beatmap(
                            id = it[Beatmaps.id].value.toString(),
                            difficultyName = it[Beatmaps.difficultyName],
                            artist = it[Beatmaps.artist],
                            artistUnicode = it[Beatmaps.artistUnicode],
                            title = it[Beatmaps.title],
                            titleUnicode = it[Beatmaps.titleUnicode],
                            creator = it[Beatmaps.creator],
                            audioUrl = "http://localhost:3001/api/v1/blobs/" + it[Beatmaps.audioKey],
                            backgroundUrl = it[Beatmaps.backgroundKey]?.let { key ->
                                "http://localhost:3001/api/v1/blobs/$key"
                            },
                        )
                    }
                )
            }
    }
}