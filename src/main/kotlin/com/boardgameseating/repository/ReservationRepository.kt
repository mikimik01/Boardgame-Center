package com.boardgameseating.repository

import com.datastax.oss.driver.api.core.cql.SimpleStatement
import com.datastax.oss.driver.api.core.CqlSession
import com.boardgameseating.model.Reservation
import java.util.UUID

class ReservationRepository(private val session: CqlSession) {

    fun createTable() {
        val statement = SimpleStatement.newInstance(
            """
            CREATE TABLE IF NOT EXISTS reservations (
                id UUID PRIMARY KEY,  
                time TEXT,
                player_id UUID,   -- ✅ Added player_id
                game_id UUID,     -- ✅ Added game_id
                requirements TEXT
            )
            """.trimIndent()
        )
        session.execute(statement)
    }

    fun addReservation(reservation: Reservation): Boolean {
        return try {
            val statement = SimpleStatement.newInstance(
                "INSERT INTO reservations (id, time, player_id, game_id, requirements) VALUES (?, ?, ?, ?, ?)",
                UUID.fromString(reservation.id),
                reservation.time,
                UUID.fromString(reservation.playerId),  // ✅ Store player_id correctly
                UUID.fromString(reservation.gameId),    // ✅ Store game_id correctly
                reservation.requirements
            )
            session.execute(statement)
            println("✅ Reservation added: $reservation")
            true
        } catch (e: Exception) {
            println("❌ Error adding reservation: ${e.message}")
            false
        }
    }

    fun findAll(): List<Map<String, String>> {
        return try {
            val result = session.execute("SELECT id, time, player_id, game_id, requirements FROM reservations")
            result.map {
                mapOf(
                    "id" to it.getUuid("id").toString(),
                    "time" to it.getString("time")!!,
                    "player_id" to it.getUuid("player_id").toString(),  // ✅ Now correctly retrieving player_id
                    "game_id" to it.getUuid("game_id").toString(),      // ✅ Now correctly retrieving game_id
                    "requirements" to (it.getString("requirements") ?: "None")
                )
            }.toList()
        } catch (e: Exception) {
            println("ℹ️ No reservations found.")
            emptyList()
        }
    }

    fun findById(id: String): Reservation? {
        return try {
            val statement = SimpleStatement.newInstance(
                "SELECT * FROM reservations WHERE id = ?",
                UUID.fromString(id)
            )
            val row = session.execute(statement).one() ?: return null
            Reservation(
                row.getUuid("id").toString(),
                row.getString("time")!!,
                row.getUuid("player_id").toString(),  // ✅ Retrieve player_id correctly
                row.getUuid("game_id").toString(),    // ✅ Retrieve game_id correctly
                row.getString("requirements")
            )
        } catch (e: Exception) {
            println("❌ Error finding reservation by ID: ${e.message}")
            null
        }
    }

    fun deleteById(id: String): Boolean {
        return try {
            val statement = SimpleStatement.newInstance(
                "DELETE FROM reservations WHERE id = ?",
                UUID.fromString(id)
            )
            session.execute(statement)
            println("🗑️ Reservation with ID '$id' deleted.")
            true
        } catch (e: Exception) {
            println("❌ Error deleting reservation: ${e.message}")
            false
        }
    }
}
