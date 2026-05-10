# 🏰 Citadels — Real-time Multiplayer Board Game

[![CI](https://github.com/StellaYe1130/CitadelsWebGame/actions/workflows/ci.yml/badge.svg)](https://github.com/StellaYe1130/CitadelsWebGame/actions/workflows/ci.yml)
![Coverage](.github/badges/jacoco.svg)
![Branches](.github/badges/branches.svg)

[中文 README](README.zh-CN.md)

A full-stack implementation of the **Citadels** card game, built on top of a university assignment (USYD INFO1113) and extended into a production-grade real-time web application.

Players build medieval cities by selecting character roles, collecting gold, constructing districts, and using unique character powers to interfere with rivals. The game supports **1–4 human players** with the remaining slots filled by AI opponents.

### Live Demo

Play online: **https://citadelswebgame.onrender.com/**

Note: the free Render instance may take about one minute to wake up after inactivity.

### Features

- **Real-time multiplayer** over WebSocket (STOMP + SockJS) — share a 6-character room code with friends
- **Solo vs AI** — play against up to 6 AI opponents with heuristic role-selection and build strategies
- **Auto-advancing AI** — server processes all AI turns server-side; the browser only blocks on human input
- **Complete game rules**: all 8 character abilities (Assassin, Thief, Magician, King, Bishop, Merchant, Architect, Warlord), full scoring (district cost, 5-colour bonus, first-complete bonus, special buildings), and purple special cards
- **Responsive dark-themed UI** built with React 18 (CDN, no build toolchain)
- **Save / load** — persist and restore game state to JSON at any point

### Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 8 · Spring Boot 2.7.x |
| Real-time | Spring WebSocket · STOMP · SockJS |
| REST | Spring MVC (`@RestController`) |
| Frontend | React 18 (UMD) · Babel Standalone · Vanilla CSS |
| Build | Gradle 8 |
| Testing | JUnit 5 · JaCoCo |
| Deployment | Docker · Docker Compose |

### Architecture

```
Browser (React 18)
  │  HTTP POST /api/lobby/create|join   ← lobby flow
  │  WebSocket /ws  (SockJS + STOMP)
  │    subscribe  /topic/game/{gameId}  ← server pushes state
  │    publish    /app/game/{gameId}/action
  │
Spring Boot
  ├── LobbyController          REST: create/join rooms
  ├── GameWebSocketController  STOMP: receive actions, request state
  ├── GameRoomManager          @Service: room registry + broadcast
  ├── WebGameService           game state machine (per room, not singleton)
  └── WebSocketConfig          STOMP broker config
```

**Key design decisions:**
- `WebGameService` is instantiated per room (not a singleton), enabling concurrent games
- `autoAdvance()` runs all AI turns synchronously inside the request thread; the broadcast fires once when a human action is needed — no polling, no timers
- `GameRoomManager` uses `synchronized` for room creation/joining and `SimpMessagingTemplate` for fan-out to all subscribers
- Role-to-player lookup uses a reverse `Map<Player, Role>` built once per `getState()` call, avoiding an O(n²) scan

### Testing

```bash
# Run all tests
gradle test

# Run tests + generate coverage report
gradle test jacocoTestReport
# Report: build/reports/jacoco/test/html/index.html
```

| Scope | Tests | Instruction Coverage | Branch Coverage |
|-------|-------|---------------------|-----------------|
| Core game logic (`citadels.*`) | 181 | 84% | 75% |
| Web layer (`citadels.web.*`) | REST controller + room manager integration tests | CI verified | CI verified |

Tests cover: game rules, all 8 character abilities, purple district effects, scoring, save/load, role selection, turn flow, AI interactions, lobby endpoints, room creation/joining, game startup, broadcasting, and edge cases.

### Getting Started

**Prerequisites:** Java 8+, Gradle 8

```bash
# Clone and build
git clone <repo-url>
cd Citadel
gradle bootJar

# Run
java -jar build/libs/citadels.jar

# Open in browser
open http://localhost:8080
```

**Docker:**

```bash
# Build and run with Docker
docker build -t citadels .
docker run --rm -p 8080:8080 citadels

# Or use Docker Compose
docker compose up --build
```

**Solo game:** click *Solo vs AI* → choose player count → Start

**Multiplayer:** click *Multiplayer* → *Create Room* → share the 6-character code → other players click *Join* and enter the code → game starts automatically when all human slots are filled

### Project Structure

```
src/main/java/citadels/
├── Card.java         card model
├── Deck.java         deck with draw / shuffle
├── Game.java         player list + turn index
├── Player.java       per-player state (gold, hand, built districts)
├── Role.java         all 8 character roles with abilities
└── web/
    ├── WebSocketConfig.java          STOMP broker setup
    ├── WebGameService.java           game state machine (per room)
    ├── GameRoom.java                 per-room wrapper
    ├── GameRoomManager.java          room registry + WS broadcast
    ├── LobbyController.java          HTTP create/join endpoints
    ├── GameWebSocketController.java  STOMP message handlers
    └── GameStateDTO.java             JSON response shape

src/main/resources/static/index.html  ← entire React frontend (single file)
src/test/java/citadels/SampleTest.java  ← 181 unit tests
```

---

*Built on top of INFO1113 Assignment 2, USYD (2024) · Extended with Spring Boot + WebSocket*
