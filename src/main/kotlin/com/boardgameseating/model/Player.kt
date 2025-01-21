package com.boardgameseating.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Player(
    @SerialName("player_id") val playerId: String,
    val name: String,
    val age: Int,
    val preferences: String
)
