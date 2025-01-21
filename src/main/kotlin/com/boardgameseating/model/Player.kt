package com.boardgameseating.model

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val playerId: String,
    val name: String,
    val age: Int,
    val preferences: String
)
