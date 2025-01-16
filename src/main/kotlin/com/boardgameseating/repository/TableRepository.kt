package com.boardgameseating.repository

import com.datastax.oss.driver.api.core.cql.SimpleStatement
import com.datastax.oss.driver.api.core.CqlSession
import com.boardgameseating.model.Table
import com.datastax.oss.driver.api.core.cql.ResultSet

class TableRepository(private val session: CqlSession) {

    fun createTable() {
        val statement = SimpleStatement.newInstance(
            """
            CREATE TABLE IF NOT EXISTS tables (
                table_id TEXT PRIMARY KEY,
                game_id TEXT,
                player_ids LIST<TEXT>
            )
            """.trimIndent()
        )
        session.execute(statement)
    }

    fun addTable(table: Table) {
        val statement = SimpleStatement.newInstance(
            "INSERT INTO tables (table_id, game_id, player_ids) VALUES (?, ?, ?)",
            table.tableId,
            table.gameId,
            table.playerIds
        )
        session.execute(statement)
    }

    fun findAll(): List<Table> {
        val result: ResultSet = session.execute("SELECT * FROM tables")

        return result.map { row ->
            val playerIds = row.getList("player_ids", String::class.java)
                ?.toList()             // Konwersja z MutableList<String> na List<String>
                ?: emptyList()         // Zwracamy pustą listę, jeżeli jest null

            Table(
                row.getString("table_id")!!,
                row.getString("game_id")!!,
                playerIds
            )
        }.toList()
    }

    fun findById(tableId: String): Table? {
        val statement = SimpleStatement.newInstance(
            "SELECT * FROM tables WHERE table_id = ?", tableId
        )
        val row = session.execute(statement).one() ?: return null

        val playerIds = row
            .getList("player_ids", String::class.java)  // => MutableList<String>?
            ?.toList()                                  // => List<String> (niemutowalna)
            ?: emptyList()                              // => jeśli null, to pusta lista

        return Table(
            tableId = row.getString("table_id")!!,
            gameId = row.getString("game_id")!!,
            playerIds = playerIds
        )
    }
}
