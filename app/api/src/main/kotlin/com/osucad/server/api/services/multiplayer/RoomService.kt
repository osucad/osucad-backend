package com.osucad.server.api.services.multiplayer

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.osucad.server.api.config.JwtConfig
import com.osucad.server.api.database.*
import com.osucad.server.api.domain.User
import com.osucad.server.api.services.RedisService
import com.osucad.server.multiplayer.redis.RedisOpsManager
import com.osucad.server.multiplayer.redis.RedisRoomResources
import com.osucad.server.multiplayer.types.AssetInfo
import com.osucad.server.multiplayer.types.SequenceNumber
import com.osucad.server.multiplayer.types.SummaryMessage
import kotlinx.coroutines.future.await
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

@Single
class RoomService(
    private val redis: RedisService,
    @Named("multiplayerJwtConfig")
    private val jwtConfig: JwtConfig,
) {
    suspend fun createToken(beatmapId: UUID, user: User): String {
        val roomId = dbQuery(isolation = Isolation.Serializable) {
            ensureRoomExists(beatmapId)
        }

        return JWT.create()
            .withSubject(user.id.toString())
            .withClaim("name", user.username)
            .withClaim("roomId", roomId.toString())
            .withExpiresAt(Instant.now().plusSeconds(60))
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .sign(Algorithm.HMAC256(jwtConfig.secret))
    }

    private suspend fun ensureRoomExists(beatmapId: UUID): UUID {
        val row = Rooms.selectAll().where { Rooms.beatmapId eq beatmapId }.singleOrNull()

        return when {
            row != null -> row[Rooms.id].value
            else -> createRoom(beatmapId)
        }
    }

    private suspend fun createRoom(beatmapId: UUID): UUID {
        val roomId = Rooms.insertAndGetId {
            it[Rooms.beatmapId] = beatmapId
        }

        val snapshotRow = BeatmapSnapshots.innerJoin(BeatmapSetSnapshots)
            .select(BeatmapSnapshots.jsonContent, BeatmapSetSnapshots.id)
            .where { BeatmapSnapshots.beatmapId eq beatmapId }
            .orderBy(BeatmapSetSnapshots.createdAt to SortOrder.DESC)
            .singleOrNull()

        if (snapshotRow == null)
            throw Exception("Could not find snapshot for beatmap $beatmapId")



        val assets = BeatmapSetAssets.selectAll()
            .where { BeatmapSetAssets.snapshotId eq snapshotRow[BeatmapSetSnapshots.id] }

        val jsonContent = snapshotRow[BeatmapSnapshots.jsonContent]

        val summary = SummaryMessage(
            clientId = -1,
            sequenceNumber = SequenceNumber(0),
            summary = jsonContent,
            assets = assets.map { AssetInfo(it[BeatmapSetAssets.filename], it[BeatmapSetAssets.key]) },
        )

        val resources = RedisRoomResources(redis.redis, roomId.value)

        val lock = resources.getLock()

        val threadId = Thread.currentThread().threadId()

        lock.lockAsync(1, TimeUnit.SECONDS, threadId)
            .toCompletableFuture()
            .await()

        val opsManager = RedisOpsManager(resources)

        opsManager.initializeFromSummary(summary)

        lock.unlockAsync(threadId)
            .toCompletableFuture()
            .await()

        return roomId.value
    }
}