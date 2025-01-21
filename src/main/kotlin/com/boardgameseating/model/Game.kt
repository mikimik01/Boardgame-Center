package com.boardgameseating.model

import kotlinx.serialization.Serializable

@Serializable

data class Game(
    val gameId: String,
    val name: String,
    val genre: String
)