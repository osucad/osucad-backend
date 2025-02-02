package com.osucad.server.api.services.beatmaps

import com.osucad.server.api.database.BeatmapSetAssets
import com.osucad.server.api.database.BeatmapSetSnapshots
import com.osucad.server.api.database.BeatmapSets
import com.osucad.server.api.database.BeatmapSnapshots
import com.osucad.server.api.database.Beatmaps
import com.osucad.server.api.services.assets.IBlobStorage
import io.ktor.utils.io.charsets.forName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.sql.Connection
import java.util.UUID
import java.util.zip.ZipFile
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString

@Single
class BeatmapSetImportService(
    private val parser: BeatmapParserService,
    private val blobStorage: IBlobStorage,
) : KoinComponent {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val shiftJis = Charsets.forName("Shift_JIS")

    private fun toZipFile(path: Path): ZipFile {
        return try {
            ZipFile(path.toFile())
        } catch (e: Exception) {
            logger.info("Failed to read .osz with default charset, falling back to Shift_JIS")
            ZipFile(path.toFile(), shiftJis)
        }
    }

    class OSZImportException(message: String? = null) : Exception(message)


    suspend fun import(path: Path): UUID = withContext(Dispatchers.IO) {
        logger.info("Attempting to import beatmapset from .osz file {}", path.absolutePathString())

        toZipFile(path).use { zipFile ->
            val allPaths = zipFile.entries().toList()

            val (beatmapEntries, assetEntries) = allPaths.partition { isBeatmap(it.name) }

            if (beatmapEntries.isEmpty())
                throw OSZImportException("No beatmaps present in archive")
            
            if (assetEntries.isEmpty())
                throw OSZImportException("No assets present in archive")


            val beatmaps = beatmapEntries.map {
                val text = zipFile.getInputStream(it).use { inputStream -> String(inputStream.readBytes()) }
                parser.parse(text)
            }

            newSuspendedTransaction(transactionIsolation = Connection.TRANSACTION_SERIALIZABLE) {
                val setId = BeatmapSets.insertAndGetId {}
                logger.info("Created beatmap set {}", setId)

                val beatmapSetSnapshotId = BeatmapSetSnapshots.insertAndGetId {
                    it[beatmapSetId] = setId
                }

                logger.info("Created beatmap set snapshot {}", beatmapSetSnapshotId)

                val assetMap = mutableMapOf<String, String>()

                for (asset in assetEntries) {
                    val blob = zipFile.getInputStream(asset).use { inputStream ->
                        blobStorage.put(inputStream)
                    }

                    val assetPath = asset.name

                    BeatmapSetAssets.insert {
                        it[snapshotId] = beatmapSetSnapshotId
                        it[key] = blob.key
                        it[filename] = assetPath
                    }

                    logger.info("Added asset {} ({})", assetPath, blob.key)

                    assetMap[assetPath] = blob.key
                }

                for (beatmap in beatmaps) {
                    val newBeatmapId = Beatmaps.insertAndGetId {
                        it[beatmapSetId] = setId
                        it[difficultyName] = beatmap.difficultyName
                        it[artist] = beatmap.artist
                        it[artistUnicode] = beatmap.artistUnicode
                        it[title] = beatmap.title
                        it[titleUnicode] = beatmap.titleUnicode
                        it[creator] = beatmap.creator
                        it[audioKey] = assetMap[beatmap.audioFile]!!
                        it[backgroundKey] = assetMap[beatmap.backgroundFile]
                    }

                    logger.info(
                        "Added beatmap {} - {} [{}] ({})",
                        beatmap.artist,
                        beatmap.title,
                        beatmap.difficultyName,
                        newBeatmapId
                    )

                    val beatmapSnapshotId = BeatmapSnapshots.insertAndGetId {
                        it[snapshotId] = beatmapSetSnapshotId
                        it[beatmapId] = newBeatmapId
                        it[jsonContent] = beatmap.json
                    }

                    logger.info("Added beatmap snapshot {}", beatmapSnapshotId)
                }

                setId.value
            }
        }
    }

    private fun isBeatmap(filename: String) = filename.endsWith(".osu")
}