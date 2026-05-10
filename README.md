# 🏰 Citadels — Real-time Multiplayer Board Game

[![CI](https://github.com/StellaYe1130/CitadelsWebGame/actions/workflows/ci.yml/badge.svg)](https://github.com/StellaYe1130/CitadelsWebGame/actions/workflows/ci.yml)
![Coverage](.github/badges/jacoco.svg)
![Branches](.github/badges/branches.svg)

[English](#english) · [中文](#chinese)

---

<a name="english"></a>

## English

A full-stack implementation of the **Citadels** card game, built on top of a university assignment (USYD INFO1113) and extended into a production-grade real-time web application.

Players build medieval cities by selecting character roles, collecting gold, constructing districts, and using unique character powers to interfere with rivals. The game supports **1–4 human players** with the remaining slots filled by AI opponents.

### Live Demo

Start the server and open **http://localhost:8080**

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

<a name="chinese"></a>

## 中文

基于大学作业（悉尼大学 INFO1113）扩展的全栈 **Citadels（城堡）** 桌游实现，将原始命令行程序改造为支持实时多人联机的 Web 应用。

玩家通过选择角色牌、收集金币、建造区域建筑，并使用各角色的特殊能力干扰对手，最终以城市总分决出胜负。游戏支持 **1–4 名真人玩家**，剩余位置由 AI 补充。

### 功能亮点

- **实时多人联机** — 基于 WebSocket（STOMP + SockJS），创建房间后分享 6 位房间码即可邀请好友
- **人机对战** — 最多 6 个 AI 对手，具备角色评分启发式策略和自动建造逻辑
- **服务端自动推进** — AI 回合由服务器全部处理完毕后再推送状态，前端无需轮询或手动点击
- **完整规则实现** — 全部 8 个角色技能、完整计分系统（建筑造价、五色加成、率先完成加分、特殊紫色建筑）
- **暗色主题响应式 UI** — React 18（CDN 引入，无需 npm 构建）
- **存档 / 读档** — 随时将游戏状态序列化为 JSON 文件，支持恢复

### 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 8 · Spring Boot 2.7.x |
| 实时通信 | Spring WebSocket · STOMP · SockJS |
| REST 接口 | Spring MVC |
| 前端 | React 18 (UMD) · Babel Standalone · 原生 CSS |
| 构建工具 | Gradle 8 |
| 测试 | JUnit 5 · JaCoCo |
| 部署 | Docker · Docker Compose |

### 系统架构

```
浏览器（React 18）
  │  HTTP POST /api/lobby/create|join   ← 创建/加入房间
  │  WebSocket /ws（SockJS + STOMP）
  │    订阅  /topic/game/{gameId}       ← 服务端主动推送游戏状态
  │    发布  /app/game/{gameId}/action  ← 发送玩家操作
  │
Spring Boot
  ├── LobbyController          REST：创建/加入房间
  ├── GameWebSocketController  STOMP：接收操作 & 状态刷新请求
  ├── GameRoomManager          @Service：房间注册表 + 广播
  ├── WebGameService           游戏状态机（每房间独立实例）
  └── WebSocketConfig          STOMP Broker 配置
```

**关键设计决策：**
- `WebGameService` 按房间实例化（非单例），支持多局并发
- `autoAdvance()` 在请求线程内同步跑完所有 AI 回合，仅在需要人类操作时广播一次——无轮询、无定时器
- `GameRoomManager` 用 `synchronized` 保证房间创建/加入的线程安全，并通过 `SimpMessagingTemplate` 向所有订阅者广播
- `getState()` 构建一次 `Map<Player, Role>` 反向映射，避免 O(n²) 遍历

### 测试

```bash
# 运行全部测试
gradle test

# 运行测试并生成覆盖率报告
gradle test jacocoTestReport
# 报告路径：build/reports/jacoco/test/html/index.html
```

| 范围 | 测试数 | 指令覆盖率 | 分支覆盖率 |
|------|--------|-----------|-----------|
| 核心游戏逻辑（`citadels.*`） | 181 | 84% | 75% |
| Web 层（`citadels.web.*`） | REST 控制器 + 房间管理集成测试 | CI 校验 | CI 校验 |

测试覆盖内容：游戏规则、全部 8 个角色技能、紫色特殊建筑效果、计分逻辑、存档/读档、角色选择、回合流程、AI 交互、Lobby 接口、房间创建/加入、游戏启动、广播逻辑及边界情况。

### 快速开始

**环境要求：** Java 8+、Gradle 8

```bash
# 克隆并构建
git clone <repo-url>
cd Citadel
gradle bootJar

# 启动服务
java -jar build/libs/citadels.jar

# 打开浏览器
open http://localhost:8080
```

**Docker：**

```bash
# 使用 Docker 构建并运行
docker build -t citadels .
docker run --rm -p 8080:8080 citadels

# 或使用 Docker Compose
docker compose up --build
```

**单人模式：** 点击 *Solo vs AI* → 选择玩家人数 → 开始游戏

**多人模式：** 点击 *Multiplayer* → *Create Room* → 将 6 位房间码分享给好友 → 好友点击 *Join* 并输入房间码 → 所有人类玩家就位后游戏自动开始

### 项目结构

```
src/main/java/citadels/
├── Card.java         卡牌模型
├── Deck.java         牌堆（抽牌 / 洗牌）
├── Game.java         玩家列表 + 当前回合索引
├── Player.java       玩家状态（金币、手牌、已建建筑）
├── Role.java         全部 8 个角色及技能描述
└── web/
    ├── WebSocketConfig.java          STOMP 配置
    ├── WebGameService.java           游戏状态机（每房间独立）
    ├── GameRoom.java                 单房间封装
    ├── GameRoomManager.java          房间注册 + WS 广播
    ├── LobbyController.java          HTTP 创建/加入接口
    ├── GameWebSocketController.java  STOMP 消息处理
    └── GameStateDTO.java             JSON 响应结构

src/main/resources/static/index.html     完整 React 前端（单文件）
src/test/java/citadels/SampleTest.java   181 个单元测试
```

---

*Built on top of INFO1113 Assignment 2, USYD (2024) · Extended with Spring Boot + WebSocket*
