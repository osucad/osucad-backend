package com.osucad.server.api.database

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object BeatmapSets : UUIDTable("osucad_baetmapset") {
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object Beatmaps : UUIDTable("osucad_beatmap") {
    val beatmapSetId = reference("beatmapset_id", BeatmapSets)
    val deleted = bool("deleted").default(false)

    val difficultyName = varchar("difficulty_name", 255)
    val artist = varchar("artist", 255)
    val artistUnicode = varchar("artist_unicode", 255)
    val title = varchar("title", 255)
    val titleUnicode = varchar("title_unicode", 255)
    val creator = varchar("creator", 255)
    val audioKey = char("audio_key", 40)
    val backgroundKey = char("background_key", 40).nullable()
}

object BeatmapSetSnapshots : UUIDTable("osucad_baetmapset_snapshot") {
    val beatmapSetId = reference("beatmapset_id", BeatmapSets)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object BeatmapSnapshots : UUIDTable("oscuad_beatmap_snapshot") {
    val snapshotId = reference("snapshot_id", BeatmapSetSnapshots)
    val beatmapId = reference("beatmap_id", Beatmaps)
    val jsonContent = text("json_content")
}

object BeatmapSetAssets : UUIDTable("osucad_baetmapset_asset") {
    val snapshotId = reference("snapshot_id", BeatmapSetSnapshots)
    val key = varchar("key", 40)
    val filename = varchar("filename", 255)
}