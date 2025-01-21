package com.boardgameseating.app

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import com.boardgameseating.controller.gameRoutes
import com.boardgameseating.controller.playerRoutes
import com.boardgameseating.controller.reservationRoutes
import com.boardgameseating.controller.tableRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.http.content.*
import kotlinx.serialization.json.Json

object ApplicationConfig {
    fun Application.module() {
        install(ContentNegotiation) {
            jackson()
            json(Json {
                ignoreUnknownKeys = true
            })
        }

        routing {
            get("/") {
                call.respondText("BoardGame Seating System is running.")
            }

            // Rejestrujemy ścieżki z poszczególnych kontrolerów
            staticResources("/", "static")
            gameRoutes()
            playerRoutes()
            tableRoutes()
            reservationRoutes()
        }
    }
}
