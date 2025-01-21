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
            // Fetch existing reservations for this game on the same day
            val existingReservations = session.execute(
                SimpleStatement.newInstance(
                    """
                SELECT player_id, requirements 
                FROM reservations 
                WHERE game_id = ? AND time = ? ALLOW FILTERING
                """.trimIndent(),
                    UUID.fromString(reservation.gameId),
                    reservation.time
                )
            ).all()

            // 🔹 Rule 1: If no reservations exist for this game on the given day, allow it
            if (existingReservations.isEmpty()) {
                return insertReservation(reservation)
            }

            // 🔹 Rule 2: Check if the player already has a reservation on this day
            val playerAlreadyReserved = existingReservations.any { it.getUuid("player_id").toString() == reservation.playerId }
            if (playerAlreadyReserved) {
                println("❌ Reservation failed: Player already has a reservation for this game on ${reservation.time}.")
                return false
            }

            // Fetch max players for this game
            val gameRow = session.execute(
                "SELECT max_players FROM games WHERE id = ?",
                UUID.fromString(reservation.gameId)
            ).one()

            val maxPlayers = gameRow?.getInt("max_players") ?: return false

            // 🔹 Rule 3: Check if there is still space available
            if (existingReservations.size >= maxPlayers) {
                println("❌ Reservation failed: No more slots available for this game on ${reservation.time}.")
                return false
            }

            // 🔹 Rule 4: Ensure all players in the reservation have the same game preferences
            val requestingPlayerPreferences = reservation.requirements

            val existingPlayerPreferences = existingReservations.mapNotNull { row ->
                row.getString("requirements") ?: "None"
            }

            if (existingPlayerPreferences.any { it != requestingPlayerPreferences }) {
                println("❌ Reservation failed: Player preferences do not match existing reservations.")
                return false
            }

            // If all conditions are met, insert the reservation
            return insertReservation(reservation)

        } catch (e: Exception) {
            println("❌ Error adding reservation: ${e.message}")
            false
        }
    }

    // Helper function to insert the reservation
    private fun insertReservation(reservation: Reservation): Boolean {
        return try {
            val statement = SimpleStatement.newInstance(
                "INSERT INTO reservations (id, time, player_id, game_id, requirements) VALUES (?, ?, ?, ?, ?)",
                UUID.fromString(reservation.id),
                reservation.time, // Already formatted as string
                UUID.fromString(reservation.playerId),
                UUID.fromString(reservation.gameId),
                reservation.requirements
            )
            session.execute(statement)
            println("✅ Reservation added: $reservation")
            true
        } catch (e: Exception) {
            println("❌ Error inserting reservation: ${e.message}")
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
                    "player_id" to it.getUuid("player_id").toString(),
                    "game_id" to it.getUuid("game_id").toString(),
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
                row.getUuid("player_id").toString(),
                row.getUuid("game_id").toString(),
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
