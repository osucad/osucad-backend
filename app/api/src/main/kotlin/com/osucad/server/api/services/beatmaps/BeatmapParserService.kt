package com.osucad.server.api.services.beatmaps

import com.osucad.server.api.config.OsucadEndpointConfig
import com.osucad.server.api.config.applicationHttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
class BeatmapParserService(private val endpoints: OsucadEndpointConfig) {
    @Serializable
    private data class ParseRequest(val text: String)

    private val json = Json { ignoreUnknownKeys = true }

    data class BeatmapParseResult(
        val json: String,
        val ruleset: String,

        val artist: String,
        val artistUnicode: String,
        val title: String,
        val titleUnicode: String,
        val creator: String,
        val previewTime: Double,
        val audioFile: String,
        val backgroundFile: String?,

        val hpDrainRate: Double,
        val circleSize: Double,
        val approachRate: Double,
        val overallDifficulty: Double,
        val sliderMultiplier: Double,
        val sliderTickRate: Double,

        val difficultyName: String,
    )

    suspend fun parse(fileContent: String): BeatmapParseResult {
        val response = applicationHttpClient.post("${endpoints.beatmapParser}/parse/osu") {
            contentType(ContentType.Application.Json)
            setBody(ParseRequest(text = fileContent))
        }

        val text = response.bodyAsText()

        val beatmap = this@BeatmapParserService.json.decodeFromString<BoxedBeatmap>(text)

        val metadata = beatmap.beatmap.beatmapInfo.metadata.data
        val difficulty = beatmap.beatmap.beatmapInfo.difficulty.data

        return BeatmapParseResult(
            json = text,
            ruleset = beatmap.ruleset,
            artist = metadata.artist,
            artistUnicode = metadata.artistUnicode,
            title = metadata.title,
            titleUnicode = metadata.titleUnicode,
            creator = metadata.creator,
            previewTime = metadata.previewTime,
            audioFile = metadata.audioFile,
            backgroundFile = metadata.backgroundFile,
            hpDrainRate = difficulty.hpDrainRate,
            circleSize = difficulty.circleSize,
            approachRate = difficulty.approachRate,
            overallDifficulty = difficulty.overallDifficulty,
            sliderMultiplier = difficulty.sliderMultiplier,
            sliderTickRate = difficulty.sliderTickRate,
            difficultyName = beatmap.beatmap.beatmapInfo.data.difficultyName
        )
    }

    @Serializable
    private class BoxedBeatmap(
        val ruleset: String,
        val beatmap: Beatmap,
    )

    @Serializable
    private class Summary<T>(val data: T)

    @Serializable
    private class Beatmap(val beatmapInfo: BeatmapInfoSummary)

    @Serializable
    private class BeatmapInfoSummary(
        val difficulty: Summary<BeatmapDifficulty>,
        val metadata: Summary<BeatmapMetadata>,
        val data: BeatmapInfoData
    )

    @Serializable
    private class BeatmapInfoData(
        val difficultyName: String,
    )

    @Serializable
    private class BeatmapDifficulty(
        val hpDrainRate: Double = 5.0,
        val circleSize: Double = 5.0,
        val approachRate: Double = 5.0,
        val overallDifficulty: Double = 5.0,
        val sliderMultiplier: Double = 1.4,
        val sliderTickRate: Double = 1.0,
    )

    @Serializable
    private class BeatmapMetadata(
        val title: String,
        val titleUnicode: String,
        val artist: String,
        val artistUnicode: String,
        val creator: String,
        val previewTime: Double,
        val audioFile: String,
        val backgroundFile: String? = null,
    )
}