package com.boardgameseating.repository

import com.datastax.oss.driver.api.core.cql.SimpleStatement
import com.datastax.oss.driver.api.core.CqlSession
import com.boardgameseating.model.Player
import java.util.UUID

class PlayerRepository(private val session: CqlSession) {

    fun createTable() {
        val statement = SimpleStatement.newInstance(
            """
            CREATE TABLE IF NOT EXISTS players (
                player_id UUID PRIMARY KEY,  -- Change to UUID
                name TEXT,
                age INT,
                preferences TEXT
            )
            """.trimIndent()
        )
        session.execute(statement)
    }

    fun addPlayer(player: Player): Boolean {
        return try {
            val statement = SimpleStatement.newInstance(
                "INSERT INTO players (player_id, name, age, preferences) VALUES (?, ?, ?, ?)",
                UUID.fromString(player.playerId),  // Convert String to UUID
                player.name,
                player.age,
                player.preferences
            )
            session.execute(statement)
            println("✅ Player '${player.name}' added successfully.")
            true
        } catch (e: Exception) {
            println("❌ Error adding player: ${e.message}")
            false
        }
    }

    fun findAll(): List<Map<String, String>> {
        return try {
            val result = session.execute("SELECT id, name, age, preferences FROM players")
            result.map {
                mapOf(
                    "player_id" to it.getUuid("id").toString(),
                    "name" to it.getString("name")!!,
                    "age" to it.getInt("age").toString(),
                    "preferences" to (it.getString("preferences") ?: "None")  // FIXED
                )
            }.toList()
        } catch (e: Exception) {
            println("❌ Error fetching players: ${e.message}")
            emptyList()
        }
    }


    fun findById(playerId: String): Player? {
        return try {
            val statement = SimpleStatement.newInstance(
                "SELECT * FROM players WHERE player_id = ?",
                UUID.fromString(playerId)  // Convert String to UUID
            )
            val row = session.execute(statement).one() ?: return null
            Player(
                row.getUuid("player_id").toString(),
                row.getString("name")!!,
                row.getInt("age"),
                row.getString("preferences")!!
            )
        } catch (e: Exception) {
            println("❌ Error finding player by ID: ${e.message}")
            null
        }
    }

    fun deleteById(playerId: String): Boolean {
        return try {
            val statement = SimpleStatement.newInstance(
                "DELETE FROM players WHERE player_id = ?",
                UUID.fromString(playerId)  // Convert String to UUID
            )
            session.execute(statement)
            println("🗑️ Player with ID '$playerId' deleted successfully.")
            true
        } catch (e: Exception) {
            println("❌ Error deleting player: ${e.message}")
            false
        }
    }
}
