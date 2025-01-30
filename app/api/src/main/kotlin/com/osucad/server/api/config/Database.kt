package com.osucad.server.api.config

import com.osucad.server.api.database.BeatmapSetAssets
import com.osucad.server.api.database.BeatmapSetSnapshots
import com.osucad.server.api.database.BeatmapSets
import com.osucad.server.api.database.BeatmapSnapshots
import com.osucad.server.api.database.Beatmaps
import com.osucad.server.api.database.Users
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val databaseConfig = DatabaseConfig {
        keepLoadedReferencesOutOfTransaction = true
    }

    if (developmentMode) {
        val db = Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MYSQL",
            user = "sa",
            password = "",
            databaseConfig = databaseConfig
        )

        transaction(db) {
            SchemaUtils.create(
                Users,
                BeatmapSets,
                Beatmaps,
                BeatmapSnapshots,
                BeatmapSetSnapshots,
                BeatmapSetAssets,
            )
        }
    }
}