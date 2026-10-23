# jredis

![CI](https://github.com/atuljha-tech/jredis/actions/workflows/ci.yml/badge.svg)

A Redis-compatible in-memory database built from scratch in Java. Implements the RESP protocol, 29 commands (strings, lists, sets), TTL with lazy + active expiry, and crash-safe RDB persistence using atomic writes. Containerized with Docker, tested with 29 JUnit tests, and deployed via GitHub Actions CI.

## Architecture

```mermaid
graph LR
    Client[redis-cli / any Redis client] -->|RESP over TCP| Server[RedisServer]
    Server -->|Thread per client| Handler[ClientHandler]
    Handler --> Parser[RespParser]
    Handler --> Encoder[RespEncoder]
    Handler --> Commands[CommandRegistry]
    Commands --> DB[Database]
    DB --> Store[ConcurrentHashMap]
    DB --> Expiry[Expiry Janitor]
    DB --> RDB[RdbSerializer]
    RDB --> Disk[dump.rdb]