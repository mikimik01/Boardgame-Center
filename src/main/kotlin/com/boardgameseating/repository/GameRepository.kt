package com.boardgameseating.repository

import com.datastax.oss.driver.api.core.cql.SimpleStatement
import com.datastax.oss.driver.api.core.CqlSession
import com.boardgameseating.model.Game
import java.util.UUID

class GameRepository(private val session: CqlSession) {

    fun createTable() {
        val statement = SimpleStatement.newInstance(
            """
            CREATE TABLE IF NOT EXISTS games (
                game_id UUID PRIMARY KEY,  -- Change to UUID for uniqueness
                name TEXT,
                genre TEXT
            )
            """.trimIndent()
        )
        session.execute(statement)
    }

    fun addGame(game: Game): Boolean {
        try {
            // Check if the game already exists
            val existingGame = findByName(game.name)
            if (existingGame != null) {
                println("🚨 Game with name '${game.name}' already exists!")
                return false // Game already exists, so we don't insert it again
            }

            val statement = SimpleStatement.newInstance(
                "INSERT INTO games (game_id, name, genre) VALUES (?, ?, ?)",
                UUID.fromString(game.gameId),  // Convert String to UUID
                game.name,
                game.genre
            )
            session.execute(statement)
            println("✅ Game '${game.name}' added successfully.")
            return true
        } catch (e: Exception) {
            println("❌ Error adding game: ${e.message}")
            return false
        }
    }

    fun findAll(): List<Map<String, String>> {
        return try {
            val result = session.execute("SELECT id, name, description, max_players FROM games")
            result.map {
                mapOf(
                    "game_id" to it.getUuid("id").toString(),  // Change `id` to `game_id`
                    "name" to it.getString("name")!!,
                    "description" to it.getString("description")!!,
                    "max_players" to it.getInt("max_players").toString()
                )
            }.toList()
        } catch (e: Exception) {
            println("❌ Error fetching games: ${e.message}")
            emptyList()
        }
    }

    fun findById(gameId: String): Game? {
        return try {
            val statement = SimpleStatement.newInstance(
                "SELECT * FROM games WHERE game_id = ?",
                UUID.fromString(gameId)  // Convert String to UUID
            )
            val row = session.execute(statement).one() ?: return null
            Game(
                row.getUuid("game_id").toString(),
                row.getString("name")!!,
                row.getString("genre")!!
            )
        } catch (e: Exception) {
            println("❌ Error finding game by ID: ${e.message}")
            null
        }
    }

    fun findByName(name: String): Game? {
        return try {
            val statement = SimpleStatement.newInstance(
                "SELECT * FROM games WHERE name = ?",
                name
            )
            val row = session.execute(statement).one() ?: return null
            Game(
                row.getUuid("game_id").toString(),
                row.getString("name")!!,
                row.getString("genre")!!
            )
        } catch (e: Exception) {
            println("❌ Error finding game by name: ${e.message}")
            null
        }
    }

    fun deleteGameById(gameId: String): Boolean {
        return try {
            val statement = SimpleStatement.newInstance(
                "DELETE FROM games WHERE game_id = ?",
                UUID.fromString(gameId)
            )
            session.execute(statement)
            println("🗑️ Game with ID '$gameId' deleted successfully.")
            true
        } catch (e: Exception) {
            println("❌ Error deleting game: ${e.message}")
            false
        }
    }
}
