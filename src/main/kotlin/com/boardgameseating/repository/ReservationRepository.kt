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
                id UUID PRIMARY KEY,  -- Change to UUID for uniqueness
                time TEXT,
                game TEXT,
                requirements TEXT
            )
            """.trimIndent()
        )
        session.execute(statement)
    }

    fun addReservation(reservation: Reservation): Boolean {
        return try {
            val statement = SimpleStatement.newInstance(
                "INSERT INTO reservations (id, time, game, requirements) VALUES (?, ?, ?, ?)",
                UUID.fromString(reservation.id),  // Convert id to UUID
                reservation.time,
                reservation.game,
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

    fun findAll(): List<Reservation> {
        return try {
            val result = session.execute("SELECT * FROM reservations")
            val reservations = result.map {
                Reservation(
                    it.getUuid("id").toString(),  // Convert UUID to String
                    it.getString("time")!!,
                    it.getString("game")!!,
                    it.getString("requirements")
                )
            }.toList()
            if (reservations.isEmpty()) {
                println("ℹ️ No reservations found.")
            }
            reservations
        } catch (e: Exception) {
            println("❌ Error fetching reservations: ${e.message}")
            emptyList()
        }
    }

    fun findById(id: String): Reservation? {
        return try {
            val statement = SimpleStatement.newInstance(
                "SELECT * FROM reservations WHERE id = ?",
                UUID.fromString(id)  // Convert id to UUID
            )
            val row = session.execute(statement).one() ?: return null
            Reservation(
                row.getUuid("id").toString(),
                row.getString("time")!!,
                row.getString("game")!!,
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
                UUID.fromString(id)  // Convert id to UUID
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
