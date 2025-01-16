# Boardgame-Center

Boardgame Center is a Cassandra-based application that provides a board game reservation system. It allows players to choose their favorite games, manage seats at gaming tables, and set custom preferences (e.g., “I only want to play with people above 30,” or “I only want to play with people named Andrzej”). This project uses **Kotlin**, **Ktor** for creating a REST API, and Apache Cassandra for data storage.

## Features

- **Manage Games** – Create, list, and find board games.
- **Manage Players** – Register players with names, ages, and custom preferences.
- **Manage Tables** – Assign players to tables based on their preferences and available games.
- **Cassandra Integration** – Leverages Cassandra’s distributed database for scalable data storage.
- **Kotlin + Ktor** – Uses Ktor for a simple, extensible HTTP server with JSON serialization.'

## Project Structure

```boardgame-seating/
│── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   ├── com/
│   │   │   │   ├── boardgameseating/
│   │   │   │   │   ├── app/
│   │   │   │   │   │   ├── Main.kt
│   │   │   │   │   │   ├── Application.kt
│   │   │   │   │   │
│   │   │   │   │   ├── config/
│   │   │   │   │   │   ├── CassandraConnector.kt
│   │   │   │   │   │
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Game.kt
│   │   │   │   │   │   ├── Player.kt
│   │   │   │   │   │   ├── Table.kt
│   │   │   │   │   │
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── GameRepository.kt
│   │   │   │   │   │   ├── PlayerRepository.kt
│   │   │   │   │   │   ├── TableRepository.kt
│   │   │   │   │   │
│   │   │   │   │   ├── service/
│   │   │   │   │   │   ├── TableAssignmentService.kt
│   │   │   │   │   │   ├── PlayerService.kt
│   │   │   │   │   │
│   │   │   │   │   ├── controller/
│   │   │   │   │   │   ├── GameController.kt
│   │   │   │   │   │   ├── PlayerController.kt
│   │   │   │   │   │   ├── TableController.kt
│   │   │   │   │   │
│   │   ├── resources/
│   │   │   ├── application.conf
│   │   │   ├── logback.xml
│
├── build.gradle.kts
├── settings.gradle.kts
├── README.md
```

### Key Directories

- **`app/`**: Entry point for the Ktor server (`Main.kt`, `Application.kt`).
- **`config/`**: Configurations, particularly `CassandraConnector.kt` for connecting to Cassandra.
- **`model/`**: Data classes representing domain objects (`Game`, `Player`, `Table`).
- **`repository/`**: Classes that handle interactions with Cassandra (CRUD operations).
- **`service/`**: Business logic for seat assignment, handling player logic, etc.
- **`controller/`**: Ktor routing definitions for REST endpoints (`games`, `players`, `tables`).

## Prerequisites

1. **Apache Cassandra**
   - Option A: **Docker**
     ```bash
     docker run -d --name my-cassandra -p 9042:9042 cassandra:latest
     ```
     Then create a keyspace:
     ```bash
     docker exec -it my-cassandra cqlsh
     CREATE KEYSPACE IF NOT EXISTS boardgames
     WITH replication = { 'class': 'SimpleStrategy', 'replication_factor': 1 };
     ```
   - Option B: **Local Installation**
      - Download and install Cassandra from [cassandra.apache.org](https://cassandra.apache.org/_/download.html).
      - Start it and create a keyspace similarly via `cqlsh`.

2. **Java and Kotlin**
   - JDK 11+ is recommended.
   - [IntelliJ IDEA](https://www.jetbrains.com/idea/) or another Kotlin-compatible IDE is helpful.

3. **Gradle**
   - This project uses Gradle (Kotlin DSL) to manage dependencies and build.

## Usage

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/boardgame-seating.git
cd boardgame-seating
