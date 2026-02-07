<div align="center">

<img src="https://raw.githubusercontent.com/lmqvq/Upload-image/main/img/202602062301732.png" alt="MQ AI Agent Banner" width="100%" />

# 🏋️ MQ AI Agent

### Intelligent Fitness Assistant & Multi-functional AI Agent Platform based on Spring AI + ReAct Architecture

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0--M6-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-ai)
[![Vue](https://img.shields.io/badge/Vue-3.2-4FC08D?style=for-the-badge&logo=vuedotjs&logoColor=white)](https://vuejs.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

[中文](README.md) &nbsp;|&nbsp; [Live Demo](https://cozi.chat/) &nbsp;|&nbsp; [Screenshots](#-screenshots) &nbsp;|&nbsp; [Quick Start](#-quick-start)

**If this project helps you, please give it a ⭐ Star!**

</div>

---

## 📖 Overview

**MQ AI Agent** is a full-stack AI application platform built on **Spring AI** framework and **ReAct (Reasoning + Acting)** agent architecture. The project focuses on **Intelligent Fitness Assistant** as its core use case while providing general-purpose **Multi-functional AI Agent** capabilities.

> 💡 This project also serves as a **graduation design project**, with the thesis titled "Design and Implementation of an Intelligent Fitness Assistant System Based on Large Language Models and ReAct Architecture".

### 🎯 In One Sentence

Integrate **Large Language Models**, **RAG Knowledge Base**, **Tool Calling**, and **Conversation Memory** into an out-of-the-box fitness assistant platform, making AI your personal fitness coach.

---

## ✨ Key Features

<table>
<tr>
<td width="50%">

**🤖 Dual AI Agents**
- **AI Fitness Coach (KeepApp)**: Professional fitness guidance + RAG knowledge base
- **AI Super Agent (MqManus)**: ReAct architecture + 7 tool integrations

</td>
<td width="50%">

**🔄 Real-time Streaming & Tool Status Display**
- SSE (Server-Sent Events) based streaming response
- ChatGPT-like typewriter effect conversation experience
- Real-time display of AI thinking process and tool execution progress
- Collapsible tool execution cards (search, file operations, etc.)

</td>
</tr>
<tr>
<td>

**📚 RAG Knowledge Base**
- 6 categories of professional fitness knowledge documents
- Supports local vector storage + Alibaba Cloud Knowledge Base
- 88%+ knowledge Q&A accuracy

</td>
<td>

**🔧 Rich Tool Ecosystem**
- File operations · Web search · Web crawling
- Resource download · PDF generation · Google search
- Extensible custom tools

</td>
</tr>
<tr>
<td>

**📊 Fitness Data Management**
- Weight / Body fat / BMI tracking
- ECharts visualization & trend analysis
- Exercise records & calorie statistics

</td>
<td>

**🧠 Conversation Memory**
- MySQL persistent multi-turn dialogue context
- Redis cache acceleration (optional)
- User-level data isolation

</td>
</tr>
<tr>
<td>

**🔐 Complete User System**
- Registration / Login / Access control
- Profile center + Avatar upload (COS)
- Fitness leaderboard

</td>
<td>

**🎨 Multi-model Support**
- Qwen (built-in) + DeepSeek + GLM + Gemini
- Supports any OpenAI-compatible models
- Configuration-based integration, no code changes

</td>
</tr>
</table>

---

## 📸 Screenshots

<details open>
<summary><b>Click to expand / collapse screenshots</b></summary>

<br>

| Landing Page | Login Page |
|:---:|:---:|
| ![Landing](https://raw.githubusercontent.com/lmqvq/Upload-image/main/img/202602062301732.png) | ![Login](https://raw.githubusercontent.com/lmqvq/Upload-image/main/mq-ai-agent/202602062303805.png) |

| Home | AI Fitness Coach |
|:---:|:---:|
| ![Home](https://raw.githubusercontent.com/lmqvq/Upload-image/main/mq-ai-agent/202602062318600.png) | ![AI Fitness Coach](https://raw.githubusercontent.com/lmqvq/Upload-image/main/mq-ai-agent/202602062306626.png) |

| AI Super Agent | Profile |
|:---:|:---:|
| ![AI Super Agent](https://raw.githubusercontent.com/lmqvq/Upload-image/main/mq-ai-agent/202602062311705.png) | ![Profile](https://raw.githubusercontent.com/lmqvq/Upload-image/main/mq-ai-agent/202602062316305.png) |

| Fitness Leaderboard | Fitness Knowledge Base |
|:---:|:---:|
| ![Leaderboard](https://raw.githubusercontent.com/lmqvq/Upload-image/main/mq-ai-agent/202602062336041.png) | ![Knowledge Base](https://raw.githubusercontent.com/lmqvq/Upload-image/main/mq-ai-agent/202602062316651.png) |

</details>

---

## 🏗️ System Architecture

### Overall Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Layer                              │
│            Vue 3 + Arco Design + ECharts + Pinia                 │
├─────────────────────────────────────────────────────────────────┤
│                     Gateway Layer                                │
│         Spring Boot Web · Auth · CORS · Rate Limiting            │
├─────────────────────────────────────────────────────────────────┤
│                    Service Layer                                 │
│      KeepApp · MqManus · UserService · FitnessService ...        │
├─────────────────────────────────────────────────────────────────┤
│                   Agent Architecture Layer                       │
│     BaseAgent → ReActAgent → ToolCallAgent → MqManus             │
│     Think → Act → Observe Loop                                   │
├─────────────┬─────────────┬───────────────┬─────────────────────┤
│  Spring AI  │ RAG Knowledge │  Chat Memory    │  Tools (7 types)    │
│  ChatClient │ Vector Search │  MySQL+Redis    │  Search/File/PDF... │
├─────────────┴─────────────┴───────────────┴─────────────────────┤
│                     Data Layer                                   │
│           MySQL 8.0 · Redis · Tencent COS · Vector Store         │
└─────────────────────────────────────────────────────────────────┘
```

### Architecture Diagram

![System Architecture](https://mq-picture-1324656182.cos.ap-guangzhou.myqcloud.com/img/202508101625250.png)

### AI Conversation Data Flow

![AI Data Flow](https://mq-picture-1324656182.cos.ap-guangzhou.myqcloud.com/img/202508101552167.png)

### ReAct Agent Workflow

```
User Request ──→ Think ──→ Need Tools? ──Yes──→ Act (Execute Tool) ──→ Observe ──→ Continue Loop
                 │                                                              │
                 └── No ──→ Generate Answer ──→ Return Result ←── Task Complete ←┘
```

---

## 🛠️ Tech Stack

### Backend

| Technology | Version | Description |
|------------|---------|-------------|
| **Java** | 21 | Programming Language |
| **Spring Boot** | 3.4.6 | Application Framework |
| **Spring AI** | 1.0.0-M6.1 | AI Development Framework |
| **MySQL** | 8.0+ | Relational Database |
| **MyBatis-Plus** | 3.5.12 | ORM Framework |
| **Redis** | 6.0+ | Cache (Optional) |
| **Qwen DashScope** | - | Default AI Model |
| **Spring AI OpenAI** | 1.0.0-M6 | OpenAI-compatible Model Access |
| **Tencent COS** | - | Object Storage |

### Frontend

| Technology | Version | Description |
|------------|---------|-------------|
| **Vue** | 3.2.13 | Frontend Framework (Composition API) |
| **Arco Design Vue** | 2.57.0 | UI Component Library |
| **Vue Router** | 4.5.1 | Routing |
| **Pinia** | 3.0.3 | State Management |
| **Axios** | 1.10.0 | HTTP Client |
| **ECharts** | 6.0.0 | Data Visualization |
| **Mermaid** | 11.9.0 | Diagram Rendering |
| **SCSS** | - | CSS Preprocessor |

---

## 📁 Project Structure

```
mq-ai-agent/
├── 📂 agent/                  # 🤖 Agent Core Architecture
│   │   ├── BaseAgent.java         #    Base Agent (State Machine + Execution Loop + SSE Events)
│   │   ├── ReActAgent.java        #    ReAct Pattern (Think-Act-Observe)
│   │   ├── ToolCallAgent.java     #    Tool Calling Agent (Real-time Progress Push)
│   │   └── MqManus.java           #    Multi-functional Agent Instance
│   ├── 📂 app/                    # 💪 Fitness Application
│   │   └── KeepApp.java           #    AI Fitness Coach
│   ├── 📂 tools/                  # 🔧 Tool Set (7 tools)
│   ├── 📂 rag/                    # 📚 RAG Knowledge Base Config
│   ├── 📂 chatmemory/             # 🧠 Chat Memory (MySQL + Redis)
│   ├── 📂 ai/                     # 🎛️ Multi-model Routing
│   ├── 📂 model/                  # 📦 Data Models
│   │   ├── dto/AgentSseEvent.java #    SSE Event DTO
│   │   └── enums/SseEventType.java#    SSE Event Type Enum
│   ├── 📂 controller/             # 🌐 API Controllers
│   ├── 📂 service/                # ⚙️ Business Services
│   └── 📂 config/                 # ⚙️ Configuration
├── 📂 mq-ai-agent-frontend/       # 🎨 Vue 3 Frontend
│   ├── 📂 src/components/         #    Components
│   │   └── ToolCallCard.vue       #    Tool Call Card Component
│   └── 📂 src/services/           #    API Services
│       └── sseParser.js           #    SSE Message Parser
├── 📂 sql/                        # 🗃️ Database Scripts
│   ├── init_all.sql               #    Complete init script (Recommended)
│   ├── create_table.sql           #    Basic business tables
│   ├── fitness_knowledge_tables.sql #  Fitness knowledge tables
│   └── knowledge_init_data.sql    #    Knowledge test data
├── 📂 docs/                       # 📄 Documentation
└── 📂 scripts/                    # 📜 Deployment Scripts
```

---

## 🚀 Quick Start

### Requirements

| Environment | Version | Required |
|-------------|---------|----------|
| Java | 21+ | ✅ |
| Maven | 3.8+ | ✅ |
| Node.js | 14+ | ✅ |
| MySQL | 8.0+ | ✅ |
| Redis | 6.0+ | ❌ (Optional) |

### 1. Clone Repository

```bash
git clone https://github.com/lmqvq/mq-ai-agent.git
cd mq-ai-agent
```

### 2. Start Backend

```bash
# 2.1 Initialize Database (one-click setup for all tables and test data)
mysql -u root -p < sql/init_all.sql

# 2.2 Configure application.yml
# Edit database connection, AI model API Key, etc.

# 2.3 Start Backend
mvn spring-boot:run
```

> 📦 **Database Note**: `sql/init_all.sql` contains complete schema for 10 tables with test data. Run once to complete initialization.

### 3. Start Frontend

```bash
cd mq-ai-agent-frontend

# 3.1 Install Dependencies
npm install

# 3.2 Start Development Server
npm run serve
```

### 4. Access Application

- 🌐 Frontend: http://localhost:8080
- 📡 Backend API: http://localhost:8123/api

### ⚙️ Core Configuration

Configure in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mq_ai_agent
    username: root
    password: your_password
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}    # Alibaba Qwen API Key

# Multi-model Configuration (Optional)
mq:
  ai:
    default-model: qwen-plus
    models:
      deepseek:
        name: "DeepSeek"
        api-key: ${DEEPSEEK_API_KEY}
        base-url: https://api.deepseek.com
        model: deepseek-chat
        enabled: true

# Search API (for Super Agent)
search-api:
  api-key: ${SEARCH_API_KEY}

# Tencent COS (for avatar upload)
cos:
  client:
    accessKey: ${COS_ACCESS_KEY}
    secretKey: ${COS_SECRET_KEY}
    region: ${COS_REGION}
    bucket: ${COS_BUCKET}
```

> 💡 It's recommended to use environment variables for sensitive information.

---

## 🤖 Agent Architecture Details

### Four-layer Inheritance Structure

```
BaseAgent (Base Agent)
  ├── State Management: IDLE → RUNNING → FINISHED/ERROR
  ├── Execution Loop: Max steps control, prevents infinite loops
  ├── Sync / Streaming execution modes
  └── SSE Event Push (8 event types)
       │
       ▼
ReActAgent (ReAct Pattern)
  ├── think(): Reasoning phase — analyze task, decide if action needed
  └── act(): Action phase — execute operations, return results
       │
       ▼
ToolCallAgent (Tool Calling)
  ├── ToolCallingManager
  ├── Automatic tool discovery and registration
  ├── Tool execution result feedback
  └── Real-time tool status push (thinking/tool_start/tool_complete/tool_error)
       │
       ▼
MqManus (Multi-functional Agent Instance)
  ├── Integrates 7 tools
  ├── Chat memory + ChatClient pooling
  ├── Custom system prompts
  └── Tool summary generation (readable tool execution descriptions)
```

### Built-in Tools Overview

|| Tool | Function | Use Case | Real-time Status |
||------|----------|----------|------------------|
|| 📄 FileOperationTool | File create / read / write / delete | Save fitness plans, training records | ✅ |
|| 🔍 WebSearchTool | Web information search | Search latest fitness news | ✅ |
|| 🌐 WebCrawlingTool | Web content crawling (Jsoup) | Get fitness articles, nutrition info | ✅ |
|| ⬇️ ResourceDownloadTool | Resource file download | Download exercise illustrations | ✅ |
|| 📑 PDFGenerationTool | PDF document generation (iText) | Generate fitness plan PDF reports | ✅ |
|| 🔎 GoogleWebSearchTool | Google Search (SerpApi) | High-quality search results | ✅ |
|| 🛑 TerminateTool | Terminate agent loop | Task completion signal | ✅ |

> ✅ All tools support real-time status push, users can see tool execution progress and result summaries on the frontend

---

## 🚢 Deployment Guide

### Docker Compose One-click Deployment

```yaml
# docker-compose.yml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8123:8123"
    environment:
      - DASHSCOPE_API_KEY=${DASHSCOPE_API_KEY}
      - MYSQL_URL=jdbc:mysql://mysql:3306/mq_ai_agent
    depends_on:
      - mysql
      - redis

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: mq_ai_agent
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql:/docker-entrypoint-initdb.d

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  mysql_data:
```

```bash
docker-compose up -d
```

### Nginx Reverse Proxy

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        root /var/www/frontend;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8123/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## 🤝 Contributing

We welcome all forms of contributions! Please check [CONTRIBUTING.md](CONTRIBUTING.md) for details.

```bash
# 1. Fork & Clone
git clone https://github.com/your-username/mq-ai-agent.git

# 2. Create feature branch
git checkout -b feature/your-feature

# 3. Commit changes (follow Conventional Commits)
git commit -m "feat: add your feature"

# 4. Push and create PR
git push origin feature/your-feature
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) - you are free to use, modify, and distribute.

---

## 🙏 Acknowledgments

- [Spring AI](https://spring.io/projects/spring-ai) - AI Application Framework
- [Alibaba Cloud Qwen](https://dashscope.aliyun.com/) - Large Language Model Service
- [Arco Design](https://arco.design/) - UI Component Library
- [Vue.js](https://vuejs.org/) - Frontend Framework
- [DeepSeek](https://www.deepseek.com/) - AI Model

---

## 📞 Contact

- **Author**: LMQICU
- **Email**: lmqicu@qq.com
- **GitHub**: [https://github.com/lmqvq](https://github.com/lmqvq)
- **Live Demo**: [https://cozi.chat/](https://cozi.chat/)

---

<div align="center">

**⭐ If this project helps you, please give it a Star! ⭐**

Made with ❤️ by [LMQICU](https://github.com/lmqvq)

[Back to Top](#-mq-ai-agent)

</div>
