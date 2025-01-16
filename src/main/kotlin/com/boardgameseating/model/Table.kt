package com.boardgameseating.model

data class Table(
    val tableId: String,
    val gameId: String,
    val playerIds: List<String>
)
