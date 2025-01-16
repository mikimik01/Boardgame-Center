package com.boardgameseating.service

import com.boardgameseating.model.Player
import com.boardgameseating.model.Table
import com.boardgameseating.model.Game
import java.util.UUID

class TableAssignmentService {

    /**
     * Przykładowa logika łączenia osób w stoliki na podstawie preferencji.
     * Tu możesz zaimplementować np. ograniczenia wiekowe czy imienne.
     */
    fun assignPlayersToTables(players: List<Player>, games: List<Game>): List<Table> {
        val tables = mutableListOf<Table>()

        // Grupujemy graczy po preferencjach, np. "Chcę grać w strategię".
        val groupedPlayers = players.groupBy { it.preferences }

        groupedPlayers.forEach { (_, group) ->
            // Przykład: do stołu potrzeba min. 2 osób, a max. 4
            // (Możesz modyfikować wedle własnych założeń)
            val chunkedGroups = group.chunked(4)
            chunkedGroups.forEach { subGroup ->
                // Wybierz losową grę
                val selectedGame = games.randomOrNull()
                if (selectedGame != null) {
                    val table = Table(
                        tableId = UUID.randomUUID().toString(),
                        gameId = selectedGame.gameId,
                        playerIds = subGroup.map { it.playerId }
                    )
                    tables.add(table)
                }
            }
        }

        return tables
    }
}
