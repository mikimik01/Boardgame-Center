package com.boardgameseating.repository

import com.datastax.oss.driver.api.core.cql.SimpleStatement
import com.datastax.oss.driver.api.core.CqlSession
import com.boardgameseating.model.Player

class PlayerRepository(private val session: CqlSession) {

    fun createTable() {
        val statement = SimpleStatement.newInstance(
            """
            CREATE TABLE IF NOT EXISTS players (
                player_id TEXT PRIMARY KEY,
                name TEXT,
                age INT,
                preferences TEXT
            )
            """.trimIndent()
        )
        session.execute(statement)
    }

    fun addPlayer(player: Player) {
        val statement = SimpleStatement.newInstance(
            "INSERT INTO players (player_id, name, age, preferences) VALUES (?, ?, ?, ?)",
            player.playerId,
            player.name,
            player.age,
            player.preferences
        )
        session.execute(statement)
    }

    fun findAll(): List<Player> {
        val result = session.execute("SELECT * FROM players")
        return result.map {
            Player(
                it.getString("player_id")!!,
                it.getString("name")!!,
                it.getInt("age"),
                it.getString("preferences")!!
            )
        }.toList()
    }

    fun findById(playerId: String): Player? {
        val statement = SimpleStatement.newInstance(
            "SELECT * FROM players WHERE player_id = ?",
            playerId
        )
        val row = session.execute(statement).one() ?: return null
        return Player(
            row.getString("player_id")!!,
            row.getString("name")!!,
            row.getInt("age"),
            row.getString("preferences")!!
        )
    }
}
