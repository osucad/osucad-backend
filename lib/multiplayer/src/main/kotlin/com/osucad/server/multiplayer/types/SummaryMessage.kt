package com.osucad.server.multiplayer.types

import kotlinx.serialization.Serializable

@Serializable
class SummaryMessage(
    val clientId: Int,
    val sequenceNumber: SequenceNumber,
    val summary: String,
    val assets: List<AssetInfo>,
)

@Serializable
class AssetInfo(
    val path: String,
    val id: String,
)