package com.boardgameseating.app

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import com.boardgameseating.controller.gameRoutes
import com.boardgameseating.controller.playerRoutes
import com.boardgameseating.controller.tableRoutes

object ApplicationConfig {
    fun Application.module() {
        install(ContentNegotiation) {
            jackson()
        }

        routing {
            get("/") {
                call.respondText("BoardGame Seating System is running.")
            }

            // Rejestrujemy ścieżki z poszczególnych kontrolerów
            gameRoutes()
            playerRoutes()
            tableRoutes()
        }
    }
}
