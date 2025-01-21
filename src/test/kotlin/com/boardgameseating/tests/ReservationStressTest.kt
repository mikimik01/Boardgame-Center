package com.boardgameseating.tests

import com.boardgameseating.model.Reservation
import com.boardgameseating.repository.ReservationRepository
import com.datastax.oss.driver.api.core.CqlSession
import kotlinx.coroutines.*
import org.junit.jupiter.api.*
import java.util.*
import kotlin.system.measureTimeMillis
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReservationStressTest {

    private lateinit var session: CqlSession
    private lateinit var reservationRepo: ReservationRepository

    @BeforeAll
    fun setup() {
        session = CqlSession.builder().build() // Connect to Cassandra
        session.execute(
            """
        CREATE KEYSPACE IF NOT EXISTS boardgames 
        WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
        """.trimIndent()
        )

        session.execute("USE boardgames;") // Ensure we are in the correct keyspace

        reservationRepo = ReservationRepository(session)

        session.execute(
            """
        CREATE TABLE IF NOT EXISTS reservations (
            id UUID PRIMARY KEY,  
            time TEXT,
            player_id UUID,
            game_id UUID,
            requirements TEXT
        );
        """.trimIndent()
        )

        println("✅ Keyspace and table created successfully.")
    }

    @AfterAll
    fun teardown() {
        session.close()
    }

    @Test
    fun `test concurrent reservations under load`() = runBlocking {
        val gameId = UUID.randomUUID().toString()
        val time = "2025-01-01"

        val numUsers = 100 // Simulate 100 concurrent users
        val reservations = (1..numUsers).map {
            Reservation(
                id = UUID.randomUUID().toString(),
                time = time,
                playerId = UUID.randomUUID().toString(),
                gameId = gameId,
                requirements = "Experienced players only"
            )
        }

        val executionTime = measureTimeMillis {
            val results = reservations.map { reservation ->
                async {
                    reservationRepo.addReservation(reservation)
                }
            }.awaitAll() // Execute all reservations concurrently

            val successCount = results.count { it }
            println("✅ Successfully added $successCount / $numUsers reservations")
        }

        println("⏳ Execution Time: $executionTime ms")

        assertTrue { executionTime < 6000 } // Ensure test runs within 5 seconds
    }

    @Test
    fun `test high traffic reservations for multiple days`() = runBlocking {
        val gameId = UUID.randomUUID().toString()

        val days = listOf("2025-01-01", "2025-01-02", "2025-01-03")
        val numUsers = 50
        val reservations = days.flatMap { day ->
            (1..numUsers).map {
                Reservation(
                    id = UUID.randomUUID().toString(),
                    time = day,
                    playerId = UUID.randomUUID().toString(),
                    gameId = gameId,
                    requirements = "Quick game"
                )
            }
        }

        val executionTime = measureTimeMillis {
            val results = reservations.map { reservation ->
                async {
                    reservationRepo.addReservation(reservation)
                }
            }.awaitAll()

            val successCount = results.count { it }
            println("✅ Successfully added $successCount / ${reservations.size} reservations")
        }

        println("⏳ Execution Time: $executionTime ms")
        assertTrue { executionTime < 7000 } // Ensure test runs within 7 seconds
    }

    @Test
    fun `test max players constraint under stress`() = runBlocking {
        val gameId = UUID.randomUUID().toString()
        val time = "2025-01-01"
        val maxPlayers = 4 // Assume max players is 4

        val reservations = (1..(maxPlayers + 5)).map { // Try to exceed limit
            Reservation(
                id = UUID.randomUUID().toString(),
                time = time,
                playerId = UUID.randomUUID().toString(),
                gameId = gameId,
                requirements = "Beginner friendly"
            )
        }

        val results = reservations.map { reservation ->
            async {
                reservationRepo.addReservation(reservation)
            }
        }.awaitAll()

        val successCount = results.count { it }
        println("✅ Successfully added $successCount / ${reservations.size} reservations")
        assertTrue { successCount <= maxPlayers } // Ensure maxPlayers constraint is respected
    }


}
