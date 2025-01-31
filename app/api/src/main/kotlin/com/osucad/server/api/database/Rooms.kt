package com.osucad.server.api.database

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object Rooms : UUIDTable("osucad_room") {
    val beatmapId = reference("osucad_batmap", Beatmaps).uniqueIndex()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object RoomMembers : UUIDTable("osucad_room_member") {
    val roomId = reference("osucad_room", Rooms)
    val userId = reference("osucad_user", Users)
}