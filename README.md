# Bebula Ai Platform

A Claude-Code-like autonomous coding agent platform built with Spring Boot, Spring AI, MCP (Model Context Protocol), and IntelliJ Plugin integration.

## Architecture Overview

The AI Agent Platform is designed as a modular, distributed system that supports autonomous code modifications, shell command execution, repository analysis, and streaming responses to IntelliJ IDEA.

### Core Components

```
ai-agent-platform/
├── backend/
│   ├── common/                    # Shared DTOs, models, utilities
│   ├── mcp-server/               # Tool execution runtime
│   └── agent-orchestrator/       # AI reasoning engine
├── plugins/
│   └── intellij-plugin/          # IntelliJ IDEA client
├── infrastructure/               # Docker, monitoring, nginx
└── docs/                         # Documentation
```

### Module Dependencies

```
intellij-plugin
    ↓
agent-orchestrator
    ↓
mcp-server
    ↓
common
```

**Important**: `agent-orchestrator` MUST NOT directly depend on `mcp-server` internals. Communication happens through HTTP/WebSocket/MCP protocol.

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.8+
- IntelliJ IDEA 2023.3+
- Docker (optional, for infrastructure)

### Building the Project

```bash
# Build all modules
mvn clean install

# Build specific modules
mvn -pl backend/common clean install
mvn -pl backend/mcp-server clean install
mvn -pl backend/agent-orchestrator clean install
```

### Running the Services

#### 1. MCP Server (Port 8081)
```bash
cd backend/mcp-server
mvn spring-boot:run
```

#### 2. Agent Orchestrator (Port 8082)
```bash
cd backend/agent-orchestrator
mvn spring-boot:run
```

#### 3. IntelliJ Plugin
```bash
cd plugins/intellij-plugin
./gradlew buildPlugin
# Install in IntelliJ IDEA from Settings → Plugins → Install Plugin from Disk
```

## Module Details

### 1. Common Module
- **Purpose**: Shared library for all backend services
- **Package**: `com.aiagent.common`
- **Contents**:
  - DTOs (`ToolRequest`, `ToolResponse`)
  - Enums (`ToolType`, `ExecutionStatus`)
  - Models (`PatchOperation`, `PatchType`)
  - Exceptions (`ToolExecutionException`)
  - Constants (`McpConstants`)

### 2. MCP Server
- **Purpose**: Execution runtime and tool gateway
- **Package**: `com.aiagent.mcp`
- **Port**: 8081
- **Core Tools**:
  - `filesystem.*` - File operations
  - `shell.*` - Command execution
  - `git.*` - Git operations
  - `repo.*` - Repository intelligence
  - `patch.*` - Patch operations

### 3. Agent Orchestrator
- **Purpose**: Main AI reasoning engine
- **Package**: `com.aiagent.orchestrator`
- **Port**: 8082
- **Responsibilities**:
  - Planning and reasoning loop
  - Prompt construction and model routing
  - Task decomposition
  - Memory management
  - Streaming responses

### 4. IntelliJ Plugin
- **Purpose**: Thin IDE client
- **Package**: `com.aiagent.plugin`
- **Features**:
  - Tool window for AI interactions
  - Diff viewer for proposed changes
  - WebSocket client for real-time updates
  - IDE context collection

## Core Architectural Principles

### 1. LLMs Are Replaceable
The permanent assets are tooling, orchestration, and repository intelligence - not the model itself.

### 2. Prefer Patch-Based Editing
Never rewrite entire files. Always:
- Find minimal region
- Generate diff
- Apply patch

### 3. Minimize Prompt Context
Inject only:
- Relevant methods and symbols
- Dependencies
- Nearby lines
Never entire repositories.

### 4. Everything Must Be Async
- Tool execution is streamed and cancellable
- Non-blocking operations
- Spring WebFlux + Reactor
- WebSocket streaming

### 5. Security First
- Respect allowed paths
- Sandboxed shell commands
- Command policy enforcement
- Audit logs

## First Milestone

**SUCCESS =**
```
IntelliJ Plugin
    ↓
WebSocket
    ↓
Agent Orchestrator
    ↓
MCP Tool Execution
    ↓
Filesystem Read/Patch
    ↓
Response Streamed Back
```

## API Endpoints

### MCP Server
- `GET /api/v1/tools` - List available tools
- `POST /api/v1/tools/execute` - Execute tool
- `GET /api/v1/tools/status/{id}` - Get execution status
- `DELETE /api/v1/tools/cancel/{id}` - Cancel execution

### Agent Orchestrator
- `POST /api/v1/orchestrate/task` - Submit task
- `GET /api/v1/orchestrate/status/{id}` - Get task status
- `WebSocket /ws/stream-response` - Stream responses

## Configuration

### MCP Server (application.yml)
```yaml
mcp:
  security:
    allowed-paths: ["/workspace", "/tmp"]
    blocked-commands: ["rm -rf", "sudo"]
  execution:
    timeout-ms: 30000
    max-concurrent: 10
```

### Agent Orchestrator (application.yml)
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
    ollama:
      base-url: http://localhost:11434
```

## Development Phases

### Phase 1 — Foundation 
- [x] Common module
- [x] MCP server structure
- [x] Basic tools (filesystem.read, filesystem.write, filesystem.patch, shell.execute)

### Phase 2 — Plugin MVP 
- [x] Tool window
- [x] WebSocket client
- [x] IDE context collector
- [x] Diff viewer framework

### Phase 3 — Orchestrator 
- [x] Tool calling engine
- [x] State machine
- [x] Prompt builder
- [x] Retry loop

### Phase 4 — Repository Intelligence (Next)
- [ ] AST indexing
- [ ] Semantic search
- [ ] Symbol graph
- [ ] Call graph

## Future Enhancements

- Multi-agent collaboration
- Vector database retrieval
- Knowledge graphs
- Autonomous planning
- Repository-wide reasoning
- Local/offline models
- Distributed execution
- Agent memory systems

## Contributing

1. Follow the dependency rules strictly
2. Keep modules decoupled
3. Write comprehensive tests
4. Document new features
5. Follow Spring Boot and Kotlin conventions

## License

MIT License - see LICENSE file for details.
