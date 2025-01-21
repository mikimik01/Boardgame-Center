package com.boardgameseating.model

import kotlinx.serialization.Serializable

@Serializable
data class Reservation(
    val id: String? = null,       // ID generowane na backendzie
    val time: String,             // Czas jako String
    val game: String,             // Gra
    val requirements: String? = null // Opcjonalne wymagania
)