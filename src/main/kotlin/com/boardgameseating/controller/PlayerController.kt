package com.boardgameseating.controller

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import com.boardgameseating.config.CassandraConnector
import com.boardgameseating.repository.PlayerRepository
import com.boardgameseating.model.Player
import java.util.UUID

fun Route.playerRoutes() {
    val playerRepo = PlayerRepository(CassandraConnector.getSession())
    playerRepo.createTable()

    route("/players") {
        get {
            call.respond(playerRepo.findAll())
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond("Missing player ID")
            val player = playerRepo.findById(id)
            if (player != null) {
                call.respond(player)
            } else {
                call.respond("Player not found")
            }
        }

        post {
            val request = call.receive<Player>()
            val newPlayer = request.copy(playerId = UUID.randomUUID().toString())
            playerRepo.addPlayer(newPlayer)
            call.respond(newPlayer)
        }
    }
}
