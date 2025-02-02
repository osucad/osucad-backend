package com.osucad.server.api.services.beatmaps

import com.osucad.server.api.database.BeatmapSets
import com.osucad.server.api.database.Beatmaps
import com.osucad.server.api.database.dbQuery
import com.osucad.server.api.domain.Beatmap
import com.osucad.server.api.domain.BeatmapSet
import com.osucad.server.api.utils.BlobReference
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single
import java.util.UUID

@Single
class BeatmapSetService {
    suspend fun findAll(): List<BeatmapSet> = dbQuery {
        BeatmapSets.leftJoin(Beatmaps).selectAll()
            .where { Beatmaps.deleted eq false }
            .toList()
            .groupBy { it[BeatmapSets.id] }
            .filter { (_, rows) -> rows.isNotEmpty() }
            .map { (_, rows) -> toBeatmapset(rows) }
    }

    suspend fun findById(id: UUID): BeatmapSet? = dbQuery {
        BeatmapSets.leftJoin(Beatmaps).selectAll()
            .where {
                (BeatmapSets.id eq id)
                    .and {
                        Beatmaps.deleted eq false
                    }

            }
            .toList()
            .let(::toBeatmapset)
    }

    private fun toBeatmapset(rows: List<ResultRow>): BeatmapSet {
        val metadataRow = rows.first()

        return BeatmapSet(
            id = metadataRow[BeatmapSets.id].value.toString(),
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
                    audioFile = BlobReference(it[Beatmaps.audioKey]),
                    backgroundFile = it[Beatmaps.backgroundKey]?.let { key -> BlobReference(key) },
                )
            }
        )
    }
}