# AGENTS.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

MQ AI Agent is an AI agent platform built with **Spring Boot 3.4.6** and **Spring AI**, providing intelligent fitness coaching (KeepApp) and a multi-functional ReAct-based agent (MqManus). It uses Alibaba Cloud's Qwen (DashScope) and DeepSeek models with MySQL-based conversation memory persistence.

## Build & Run Commands

```bash
# Build project
./mvnw clean package -DskipTests

# Run application
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=KeepAppTest

# Run specific test method
./mvnw test -Dtest=KeepAppTest#testDoChat

# Generate test coverage report (outputs to target/site/jacoco/)
./mvnw jacoco:report
```

Application runs on `http://localhost:8123/api` with Swagger UI at `/swagger-ui.html`.

## Architecture

### Agent Hierarchy (ReAct Pattern)

```
BaseAgent (abstract)
  └── ReActAgent (abstract) - think() / act() cycle
        └── ToolCallAgent - Spring AI tool calling implementation
              └── MqManus - General-purpose agent with all tools
```

- **BaseAgent**: Manages agent state (`IDLE`/`RUNNING`/`FINISHED`/`ERROR`), step-based execution loop (max 10-20 steps), and SSE streaming
- **ReActAgent**: Implements `step()` as `think()` → `act()` cycle
- **ToolCallAgent**: Handles Spring AI `ToolCallback` execution via `ToolCallingManager`, disables built-in tool execution (`withProxyToolCalls(true)`)
- **MqManus**: Production agent with configurable model selection and user memory

### Key Components

| Package | Purpose |
|---------|---------|
| `agent/` | Agent implementations (BaseAgent → ReActAgent → ToolCallAgent → MqManus) |
| `app/` | Application services (KeepApp fitness assistant) |
| `chatmemory/` | MySQL-based `ChatMemory` implementation with user isolation |
| `pool/` | `ChatClientPool` - caches ChatClient instances (60min expiry, 1000 max) |
| `tools/` | Tool implementations (FileOperation, WebSearch, PDF, etc.) |
| `rag/` | RAG configuration using DashScope cloud knowledge base |
| `ai/` | `AiModelRouter` for multi-model support (qwen-plus, deepseek) |

### ChatClient Pool Pattern

All ChatClient instances are created through `ChatClientPool` which provides:
- Model-specific client creation (`getKeepAppClient()`, `getMqManusClient()`)
- Optional user memory support (`getKeepAppClientWithMemory()`)
- Automatic cache management with LRU eviction

### Tool Registration

Tools are registered in `ToolRegistration.java` as a single `ToolCallback[]` bean:
- `FileOperationTool` - file read/write
- `WebSearchTool` / `GoogleWebSearchTool` - web search via SearchAPI
- `WebCrawlingTool` - webpage content extraction (Jsoup)
- `ResourceDownloadTool` - file download
- `PDFGenerationTool` - iText PDF generation
- `TerminateTool` - signals agent completion (sets `AgentState.FINISHED`)

## Configuration

### Required Environment Variables

```yaml
# application.yml or environment
spring.ai.dashscope.api-key: ${DASHSCOPE_API_KEY}
search-api.api-key: ${SEARCH_API_KEY}
mq.ai.deepseek.api-key: ${DEEPSEEK_API_KEY}  # Optional
```

### Model Selection

The `model` parameter in API calls accepts: `qwen`, `qwen-plus`, `dashscope`, `tongyi`, `deepseek`, `deepseek-chat`. Default is `qwen-plus`.

## Database

Initialize with:
```bash
mysql -u root -p < sql/create_table.sql
```

Key tables:
- `keep_report` - stores serialized conversation messages (JSON) per chatId/userId
- `user` - user accounts with session management

## API Patterns

### Streaming Responses (SSE)

KeepApp uses Reactor `Flux<String>`:
```java
@GetMapping(value = "/keep_app/chat/sse/user", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> doChatWithKeepAppSSEUser(String message, String chatId, String model, HttpServletRequest request)
```

MqManus uses `SseEmitter` with async execution:
```java
@GetMapping("/manus/chat/user")
public SseEmitter doChatWithManusUser(String message, String chatId, String model, HttpServletRequest request)
```

### Chat Memory

Conversation ID follows pattern `chat_{conversationId}`. Memory is per-user when `userId` is provided, otherwise shared.

## Code Standards

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use Lombok annotations (`@Data`, `@Slf4j`, `@Builder`)
- 4-space indentation, 120 char line limit
- Commit messages: Conventional Commits format (`feat:`, `fix:`, `docs:`, etc.)

## Adding New Tools

1. Create tool class implementing methods annotated with `@Tool`
2. Register in `ToolRegistration.allTools()` bean
3. Tool results return as `String` - the agent observes this in its ReAct loop

## Adding New Agents

1. Extend `ToolCallAgent` (or `ReActAgent` for custom logic)
2. Override `initPrompts()` to set `name`, `systemPrompt`, `nextStepPrompt`, `maxSteps`
3. Create through constructor with `ToolCallback[]` and either `ChatModel` or `ChatClientPool`
