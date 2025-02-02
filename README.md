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
     ```bash
     systemctl start cassandra
     cqlsh
     notetool
     ```
     - Casandra na wielu węzłach, przykład konfiguracji dla:
    ```bash
      node0 10.10.0.101 (seed1)
      node1 10.10.0.102
      node2 10.10.0.103
      node3 10.10.0.104 (seed2)
      node4 10.10.0.105
      node5 10.10.0.106
    ```
    Edytuj plik konfiguracyjny /etc/cassandra/conf/cassandra.yaml:
   ```bash
         cluster_name: 'Test Cluster'       # default
         num_tokens: 256                    # default
         seed_provider:
         - class_name: org.apache.cassandra.\\
         locator.SimpleSeedProvider         # default
         parameters:
         - seeds: "10.10.0.101,10.10.0.104"
         listen_address: 10.10.0.101        # local ip
         rpc_address: localhost             # default
         endpoint_snitch: SimpleSnitch      # default
    ```

3. **Java and Kotlin**
   - JDK 11+ is recommended.
   - [IntelliJ IDEA](https://www.jetbrains.com/idea/) or another Kotlin-compatible IDE is helpful.

4. **Gradle**
   - This project uses Gradle (Kotlin DSL) to manage dependencies and build.

## Usage

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/boardgame-seating.git
cd boardgame-seating
```

### 2. Start multiple Cassandra instances on local computer

---

#### **2.1. Create a Docker network**
Create a custom Docker bridge network to allow the containers to communicate.

```bash
docker network create cassandra-cluster
```

---

#### **2.2. Start the first Cassandra node**

Launch the first Cassandra container and assign it a name (e.g., `cassandra1`).

```bash
docker run -d --name cassandra1 --network cassandra-cluster cassandra:latest
```

---

#### **2.3. Start additional Cassandra nodes**
For each additional node, specify the `CASSANDRA_SEEDS` environment variable to point to the first node (the seed node).

For example, to start the second and third nodes:

```bash
docker run -d --name cassandra2 --network cassandra-cluster \
    -e CASSANDRA_SEEDS=cassandra1 \
    cassandra:latest



docker run -d --name cassandra3 --network cassandra-cluster \
    -e CASSANDRA_SEEDS=cassandra1 \
    cassandra:latest
```

---

#### **2.4. Verify the cluster**
Once all containers are running, you can check the cluster status using the Cassandra Query Language Shell (`cqlsh`).

1. Access the `cqlsh` from one of the containers:

   ```bash
   docker exec -it cassandra1 cqlsh
   ```

2. Run the following command to view the cluster status:

   ```cql
   SELECT peer, rpc_address, data_center, rack FROM system.peers;
   ```

   This should show information about the other nodes in the cluster.

---

#### **2.5. Scaling the cluster dynamically**
To dynamically scale the cluster, you can add more nodes using the same `CASSANDRA_SEEDS` configuration. Ensure you specify one or more seed nodes for the new container.

```bash
docker run -d --name cassandra4 --network cassandra-cluster \
    -e CASSANDRA_SEEDS=cassandra1 \
    cassandra:latest
```

---

#### **2.6. Optional: Expose ports for external access**
If you need to access Cassandra nodes externally, map their ports using `-p`. For example:

```bash
docker run -d --name cassandra1 --network cassandra-cluster \
    -p 9042:9042 \
    cassandra:latest
```
