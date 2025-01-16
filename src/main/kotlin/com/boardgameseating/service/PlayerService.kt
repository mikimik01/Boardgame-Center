package com.boardgameseating.service

import com.boardgameseating.model.Player
import com.boardgameseating.repository.PlayerRepository
import java.util.UUID

class PlayerService(private val playerRepository: PlayerRepository) {

    fun createPlayer(name: String, age: Int, preferences: String): Player {
        val newPlayer = Player(
            playerId = UUID.randomUUID().toString(),
            name = name,
            age = age,
            preferences = preferences
        )
        playerRepository.addPlayer(newPlayer)
        return newPlayer
    }

    fun getAllPlayers(): List<Player> {
        return playerRepository.findAll()
    }

    fun getPlayerById(id: String): Player? {
        return playerRepository.findById(id)
    }
}
