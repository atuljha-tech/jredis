# jredis

![CI](https://github.com/atuljha-tech/jredis/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)
![Tests](https://img.shields.io/badge/Tests-29%20Passing-brightgreen.svg)

A Redis-compatible in-memory database built entirely from scratch in Java. 

**jredis** implements the raw RESP (REdis Serialization Protocol) wire protocol, 29 commands across multiple data types (strings, lists, sets), TTL with lazy + active expiry, and crash-safe RDB persistence using atomic writes. It is containerized with Docker, tested with 29 JUnit tests, and integrated with GitHub Actions CI.

---

## 📖 Table of Contents
- [Objective](#-objective)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Features](#-features)
- [Project Structure](#-project-structure)
- [Quick Start](#-quick-start)
- [Benchmark](#-benchmark)
- [Design Decisions](#-design-decisions)

---

## 🎯 Objective

The goal of this project was to demystify how high-performance databases like Redis work under the hood without relying on external libraries or frameworks. 

Instead of building a simple HTTP wrapper, `jredis` speaks the actual binary protocol that `redis-cli` and other standard Redis clients use. It solves the complex challenges of network concurrency, binary-safe data parsing, and crash-safe data persistence from first principles.

---

## 🏗 Architecture

```mermaid
graph TD
    Client[redis-cli / Standard Redis Client] -->|RESP over TCP| Server[RedisServer]
    
    subgraph Server Core
        Server -->|Spawns| Handler[ClientHandler (Thread)]
        Handler --> Parser[RespParser]
        Handler --> Encoder[RespEncoder]
        Handler --> Commands[CommandRegistry]
    end

    subgraph Data & Storage
        Commands --> DB[Database Engine]
        DB --> Store[(ConcurrentHashMap)]
        DB --> Expiry[Expiry Janitor]
        DB --> RDB[RdbSerializer]
    end

    subgraph Persistence
        RDB -->|Atomic Write| Disk[dump.rdb]
    end
```

---

## 💻 Tech Stack
- **Language:** Java 17 (utilizing Sealed Interfaces and Pattern Matching)
- **Build Tool:** Maven (multi-stage Docker build)
- **Networking:** Java Standard Library `java.net.ServerSocket` (Blocking I/O, Thread-per-client)
- **Testing:** JUnit 5 (29 automated tests)
- **CI/CD:** GitHub Actions
- **Containerization:** Docker (Eclipse Temurin Alpine JRE)

---

## ✨ Features
- **RESP Protocol:** A fully binary-safe parser and encoder that handles arrays, bulk strings, integers, and errors.
- **29 Commands Implemented:**
  - **Strings:** `PING`, `ECHO`, `SET`, `GET`, `DEL`, `EXISTS`, `TYPE`, `INCR`, `DECR`, `APPEND`, `STRLEN`
  - **Lists:** `LPUSH`, `RPUSH`, `LPOP`, `RPOP`, `LLEN`, `LRANGE`
  - **Sets:** `SADD`, `SMEMBERS`, `SISMEMBER`, `SCARD`, `SREM`
  - **Keys/TTL:** `EXPIRE`, `TTL`, `PTTL`, `PERSIST`, `DBSIZE`, `FLUSHALL`
  - **Persistence:** `SAVE`, `BGSAVE`
- **TTL (Time To Live):** Implements both lazy expiry (checks on access) and active expiry (a background janitor thread running at 10Hz).
- **Crash-Safe Persistence:** Saves data to an `.rdb` file using the atomic write pattern (`.tmp` -> `fsync` -> `rename`).
- **Type Safety:** Uses Java 17 Sealed Interfaces to model Strings, Lists, and Sets safely under the hood.
- **Command Pattern:** Every command is isolated in its own class, making the codebase extensible without modifying existing logic.

---

## 📁 Project Structure
```text
jredis/
├── .github/workflows/ci.yml      # GitHub Actions pipeline (compile + test)
├── src/
│   ├── main/java/com/redisclone/
│   │   ├── Main.java             # App entry point (parses --port argument)
│   │   ├── commands/             # Command Pattern implementation
│   │   │   ├── Command.java      # Interface all commands implement
│   │   │   ├── CommandHandler.java # Routes incoming commands to registry
│   │   │   ├── CommandRegistry.java# Maps command names to classes
│   │   │   └── impl/             # 29 individual command classes (Set, Get, Lpush, etc.)
│   │   ├── persistence/
│   │   │   └── RdbSerializer.java# Binary file read/write for snapshots
│   │   ├── protocol/
│   │   │   ├── RespEncoder.java  # Writes Java objects to binary RESP format
│   │   │   ├── RespParser.java   # Reads binary RESP format into Java Lists
│   │   │   └── RespException.java# Custom protocol error handling
│   │   ├── server/
│   │   │   ├── ClientHandler.java# Per-client thread (reads sockets, delegates)
│   │   │   └── RedisServer.java  # Opens TCP port, accepts connections
│   │   └── storage/
│   │       ├── Database.java     # Core engine (maps, TTL logic, janitor)
│   │       └── value/            # Sealed type system
│   │           ├── RedisValue.java# The sealed interface
│   │           ├── StringValue.java
│   │           ├── ListValue.java
│   │           └── SetValue.java
│   └── test/java/com/redisclone/ # 29 JUnit 5 Tests mirroring main package
├── Dockerfile                    # Multi-stage build (Maven -> Alpine JRE)
├── pom.xml                       # Dependencies (JUnit) & Shade Plugin for fat JAR
└── fly.toml                      # Cloud deployment configuration
```

---

## 🚀 Quick Start

### Option 1: Docker (Recommended)
Run the entire database instantly without installing Java or Maven.

```bash
# Build the image
docker build -t jredis .

# Run the server (maps container port to local port)
docker run --rm -p 6379:6379 jredis
```

### Option 2: Local (Maven)

```bash
# Compile, test, and package
mvn clean package

# Run the server
java -jar target/java-redis-clone-1.0.0.jar
```

### Connect and Test
Once running, open a new terminal and connect using any standard Redis client:

```bash
redis-cli -p 6379
127.0.0.1:6379> PING
PONG
127.0.0.1:6379> SET hello world
OK
127.0.0.1:6379> GET hello
"world"
```

---

## ⚡ Benchmark
Performance measured locally using `redis-benchmark` over 100,000 requests with 50 parallel connections.

```bash
redis-benchmark -h 127.0.0.1 -p 6379 -c 50 -n 100000 -t set,get -q
```

| Operation | jredis (this project) | Real Redis 7.x |
| :--- | :--- | :--- |
| **SET** | ~45,000 ops/sec | ~110,000 ops/sec |
| **GET** | ~48,000 ops/sec | ~115,000 ops/sec |

> **Note:** jredis uses a blocking Thread-per-client model for architectural clarity. Real Redis uses a highly optimized single-threaded event loop. Achieving ~40% of Redis's throughput in pure Java is a testament to the efficiency of the custom RESP parser and ConcurrentHashMap storage.

---

## 🧠 Design Decisions

- **Command Pattern over Switch Statements:**  
  Instead of a 500-line switch statement, every command (e.g., `SetCommand`, `LpushCommand`) implements the `Command` interface. This adheres to the Open/Closed Principle—adding a new command requires zero modifications to existing files, just a new class and one line in the registry.

- **Sealed Interfaces for Data Types:**  
  Java doesn't have union types. By making `RedisValue` a sealed interface with `StringValue`, `ListValue`, and `SetValue` implementations, the compiler guarantees we handle every type safely without risky casting.

- **Atomic Persistence:**  
  Writing directly to `dump.rdb` risks total data loss if the server crashes mid-write. We use the POSIX atomic pattern: write to `.tmp`, force the OS to flush the cache to disk (`fsync`), then rename the file.

- **Active vs. Lazy Expiry:**  
  If a key expires, it's deleted immediately upon the next `GET` (Lazy). However, if nobody ever calls `GET`, the memory leaks. A background "Janitor" thread sweeps the map 10 times a second to clean up abandoned keys (Active).

---

Built from scratch as a deep-dive into systems programming, network protocols, and database architecture.
