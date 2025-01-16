package com.boardgameseating.repository

import com.datastax.oss.driver.api.core.cql.SimpleStatement
import com.datastax.oss.driver.api.core.CqlSession
import com.boardgameseating.model.Table

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
        val result = session.execute("SELECT * FROM tables")
        return result.map {
            Table(
                it.getString("table_id")!!,
                it.getString("game_id")!!,
                it.getList("player_ids", String::class.java)
            )
        }.toList()
    }

    fun findById(tableId: String): Table? {
        val statement = SimpleStatement.newInstance(
            "SELECT * FROM tables WHERE table_id = ?",
            tableId
        )
        val row = session.execute(statement).one() ?: return null
        return Table(
            row.getString("table_id")!!,
            row.getString("game_id")!!,
            row.getList("player_ids", String::class.java)
        )
    }
}
