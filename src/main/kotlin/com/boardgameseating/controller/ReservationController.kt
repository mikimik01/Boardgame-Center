package com.boardgameseating.controller

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import com.boardgameseating.config.CassandraConnector
import com.boardgameseating.repository.ReservationRepository
import com.boardgameseating.model.Reservation
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import java.util.UUID

fun Route.reservationRoutes() {
    val session = CassandraConnector.getSession()
    val reservationRepo = ReservationRepository(session)

    reservationRepo.createTable()

    route("/reservations") {
        get {
            call.respond(reservationRepo.findAll())
        }

        post {
            val rawJson = call.receiveText() // Odczytaj body JSON jako tekst raz
            println("📝 Received raw JSON: $rawJson") // Logowanie przychodzących danych

            try {
                // Parsowanie JSON na obiekt
                val reservation = kotlinx.serialization.json.Json.decodeFromString<Reservation>(rawJson)
                println("✅ Successfully parsed reservation: $reservation")

                val newReservation = reservation.copy(id = UUID.randomUUID().toString())
                reservationRepo.addReservation(newReservation)

                call.respond(newReservation)
            } catch (e: Exception) {
                println("❌ Error parsing JSON: ${e.message}")
                call.respondText("Invalid JSON format", status = HttpStatusCode.BadRequest)
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respondText(
                "Missing or malformed id", status = HttpStatusCode.BadRequest
            )

            val success = reservationRepo.deleteById(id)
            if (success) {
                call.respondText("Reservation deleted", status = HttpStatusCode.OK)
            } else {
                call.respondText("Reservation not found", status = HttpStatusCode.NotFound)
            }
        }
    }
}
