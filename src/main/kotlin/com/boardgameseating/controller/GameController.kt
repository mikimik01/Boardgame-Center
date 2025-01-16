package com.boardgameseating.controller

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import com.boardgameseating.config.CassandraConnector
import com.boardgameseating.repository.GameRepository
import com.boardgameseating.model.Game
import java.util.UUID

fun Route.gameRoutes() {
    val gameRepo = GameRepository(CassandraConnector.getSession())
    gameRepo.createTable() // Tworzymy tabelę przy starcie, można to też zrobić gdzieś indziej.

    route("/games") {

        get {
            call.respond(gameRepo.findAll())
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond("Missing game ID")
            val game = gameRepo.findById(id)
            if (game != null) {
                call.respond(game)
            } else {
                call.respond("Game not found")
            }
        }

        post {
            val request = call.receive<Game>()
            val newGame = request.copy(gameId = UUID.randomUUID().toString())
            gameRepo.addGame(newGame)
            call.respond(newGame)
        }
    }
}
