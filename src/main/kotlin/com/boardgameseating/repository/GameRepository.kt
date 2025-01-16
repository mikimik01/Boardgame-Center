package com.boardgameseating.repository

import com.datastax.oss.driver.api.core.cql.SimpleStatement
import com.datastax.oss.driver.api.core.CqlSession
import com.boardgameseating.model.Game
import java.util.UUID

class GameRepository(private val session: CqlSession) {

    fun createTable() {
        val statement = SimpleStatement.newInstance(
            """
            CREATE TABLE IF NOT EXISTS games (
                game_id TEXT PRIMARY KEY,
                name TEXT,
                genre TEXT
            )
            """.trimIndent()
        )
        session.execute(statement)
    }

    fun addGame(game: Game) {
        val statement = SimpleStatement.newInstance(
            "INSERT INTO games (game_id, name, genre) VALUES (?, ?, ?)",
            game.gameId,
            game.name,
            game.genre
        )
        session.execute(statement)
    }

    fun findAll(): List<Game> {
        val result = session.execute("SELECT * FROM games")
        return result.map {
            Game(
                it.getString("game_id")!!,
                it.getString("name")!!,
                it.getString("genre")!!
            )
        }.toList()
    }

    fun findById(gameId: String): Game? {
        val statement = SimpleStatement.newInstance(
            "SELECT * FROM games WHERE game_id = ?",
            gameId
        )
        val row = session.execute(statement).one() ?: return null
        return Game(
            row.getString("game_id")!!,
            row.getString("name")!!,
            row.getString("genre")!!
        )
    }
}
