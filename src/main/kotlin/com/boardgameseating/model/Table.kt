package com.boardgameseating.model

import kotlinx.serialization.Serializable

@Serializable

data class Table(
    val tableId: String,
    val gameId: String,
    val playerIds: List<String>
)
