package com.boardgameseating.controller

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import com.boardgameseating.config.CassandraConnector
import com.boardgameseating.model.Game
import com.boardgameseating.model.Player
import com.boardgameseating.repository.TableRepository
import com.boardgameseating.repository.PlayerRepository
import com.boardgameseating.repository.GameRepository
import com.boardgameseating.model.Table
import com.boardgameseating.service.TableAssignmentService
import java.util.UUID

fun Route.tableRoutes() {
    val session = CassandraConnector.getSession()
    val tableRepo = TableRepository(session)
    val playerRepo = PlayerRepository(session)
    val gameRepo = GameRepository(session)

    tableRepo.createTable()

    route("/tables") {

        get {
            call.respond(tableRepo.findAll())
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond("Missing table ID")
            val table = tableRepo.findById(id)
            if (table != null) {
                call.respond(table)
            } else {
                call.respond("Table not found")
            }
        }

        /**
         * Endpoint pokazujący ideę przypisywania graczy do stołu
         * na podstawie preferencji i listy dostępnych gier.
         */
        post("/assign") {
            val players: List<Map<String, String>> = playerRepo.findAll()
            val games: List<Map<String, String>> = gameRepo.findAll()

            // Convert List<Map<String, String>> to List<Player>
            val mappedPlayers: List<Player> = players.map { playerMap ->
                Player(
                    playerId = playerMap["player_id"]!!,  // Ensure it exists
                    name = playerMap["name"]!!,
                    age = playerMap["age"]!!.toInt(),  // Convert string to int
                    preferences = playerMap["preferences"] ?: "None"  // Handle null case
                )
            }

            // ✅ Fixing the Game Conversion
            val mappedGames: List<Game> = games.map { gameMap ->
                Game(
                    gameId = gameMap["game_id"]!!,  // Ensure it exists
                    name = gameMap["name"]!!,
                    genre = gameMap["genre"] ?: "Unknown"  // Fix: Use the correct field name
                )
            }

            val service = TableAssignmentService()
            val generatedTables: List<Table> = service.assignPlayersToTables(mappedPlayers, mappedGames)

            // Save tables to DB
            generatedTables.forEach { tableRepo.addTable(it) }

            call.respond(generatedTables)
        }



        /**
         * Przykładowe dodanie konkretnego stołu ręcznie
         */
        post {
            val table = call.receive<Table>()
            val newTable = table.copy(tableId = UUID.randomUUID().toString())
            tableRepo.addTable(newTable)
            call.respond(newTable)
        }
    }
}
