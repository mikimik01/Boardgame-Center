package com.boardgameseating.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Game(
    @SerialName("game_id") val gameId: String,  // Maps to "game_id" in database & JSON
    val name: String,
    val genre: String
)