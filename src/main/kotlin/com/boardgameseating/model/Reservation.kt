package com.boardgameseating.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Reservation(
    @SerialName("id") val id: String = UUID.randomUUID().toString(),
    @SerialName("time") val time: String,
    @SerialName("playerId") val playerId: String,
    @SerialName("gameId") val gameId: String,
    @SerialName("requirements") val requirements: String? = null
)