<div align="center">

<img src="https://raw.githubusercontent.com/lmqvq/Upload-image/main/img/202602062301732.png" alt="MQ AI Agent Banner" width="100%" />

# 🏋️ MQ AI Agent

### 基于 Spring AI + ReAct 架构的智能健身助手 & 多功能 AI 智能体平台

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0--M6-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-ai)
[![Vue](https://img.shields.io/badge/Vue-3.2-4FC08D?style=for-the-badge&logo=vuedotjs&logoColor=white)](https://vuejs.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

[English](README_EN.md) &nbsp;|&nbsp; [在线体验](https://cozi.chat/) &nbsp;|&nbsp; [功能截图](#-功能截图) &nbsp;|&nbsp; [快速开始](#-快速开始)

**如果这个项目对你有帮助，请点个 ⭐ Star 支持一下！**

</div>

---

## 📖 项目简介

**MQ AI Agent** 是一个基于 **Spring AI** 框架和 **ReAct（Reasoning + Acting）** 智能体架构构建的全栈 AI 应用平台。项目以 **智能健身助手** 为核心应用场景，同时提供通用的 **多功能 AI 智能体** 能力。

### 🎯 一句话介绍

将 **大语言模型**、**RAG 知识库**、**工具调用**、**对话记忆** 等 AI 能力整合到一个开箱即用的健身助手平台中，让 AI 成为你的专属健身教练。

---

## ✨ 核心特性

<table>
<tr>
<td width="50%">
**🤖 双 AI 智能体**

- **AI 健身教练（KeepApp）**：专业健身指导 + RAG 知识库
- **AI 超级智能体（MqManus）**：ReAct 架构 + 7 种工具调用

</td>
<td width="50%">

**🔄 实时流式对话与工具状态展示**
- 基于 SSE（Server-Sent Events）的流式响应
- 类 ChatGPT 的打字机效果对话体验
- 实时展示 AI 思考过程和工具调用进度
- 可折叠的工具执行卡片（搜索、文件操作等）

</td>
</tr>
<tr>
<td>

**📚 RAG 知识库**
- 6 大类专业健身知识文档
- 支持本地向量存储 + 阿里云知识库双方案
- 知识问答准确率 88%+

</td>
<td>

**🔧 丰富工具生态**
- 文件操作 · 网络搜索 · 网页抓取
- 资源下载 · PDF 生成 · Google 搜索
- 支持自定义扩展

</td>
</tr>
<tr>
<td>

**📊 健身数据管理**
- 体重 / 体脂率 / BMI 数据追踪
- ECharts 可视化图表与趋势分析
- 运动记录与卡路里统计

</td>
<td>

**🧠 对话记忆系统**
- MySQL 持久化多轮对话上下文
- Redis 缓存加速（可选）
- 用户级数据隔离

</td>
</tr>
<tr>
<td>

**🔐 完整用户系统**
- 注册 / 登录 / 权限控制
- 个人中心 + 头像上传（COS）
- 健身排行榜

</td>
<td>

**🎨 多模型支持**
- 通义千问（内置）+ DeepSeek + GLM + Gemini
- 支持任意 OpenAI 兼容模型
- 配置化接入，无需改代码

</td>
</tr>
</table>

---

## 🧪 标准化评测模块（Assessment）

项目当前已内置 `assessment` 标准化评测模块，并以“大学生体测”作为第一套官方落地方案。

当前已支持：

- 大学生体测核心项目：`BMI`、`肺活量`、`50米跑`、`坐位体前屈`、`立定跳远`、`引体向上 / 仰卧起坐`、`1000米跑 / 800米跑`
- 评测画像、评测记录、评测明细、评测报告完整闭环
- 规则评分引擎 + AI 个性化建议
- 前端录入页 `/assessment/entry`
- 前端报告页 `/assessment`
- 记录新增、查看、编辑、删除、趋势分析、AI 重新生成

assessment 相关核心文件：

- `src/main/java/com/mq/mqaiagent/assessment/`
- `mq-ai-agent-frontend/src/views/AssessmentEntry.vue`
- `mq-ai-agent-frontend/src/views/AssessmentReport.vue`
- `sql/assessment_tables.sql`
- `sql/assessment_university_standard_seed.sql`
- `sql/assessment_university_standard_rules.sql`

---

## 📸 功能截图

<details open>
<summary><b>点击展开 / 收起截图</b></summary>

<br>

| 着陆页 | 登录页 |
|:---:|:---:|
| ![着陆页](https://raw.githubusercontent.com/lmqvq/Upload-image/main/img/202602062301732.png) | ![登录页](https://raw.githubusercontent.com/lmqvq/Upload-image/main/mq-ai-agent/202602062303805.png) |

| 主页 | AI 健身教练 |
|:---:|:---:|
| ![主页](https://raw.githubusercontent.com/lmqvq/Upload-image/main/mq-ai-agent/202602062318600.png) | ![AI健身教练](https://raw.githubusercontent.com/lmqvq/Upload-image/main/mq-ai-agent/202602062306626.png) |

| AI 超级智能体 | 个人中心 |
|:---:|:---:|
| ![AI超级智能体](https://raw.githubusercontent.com/lmqvq/Upload-image/main/mq-ai-agent/202602062311705.png) | ![个人中心](https://raw.githubusercontent.com/lmqvq/Upload-image/main/mq-ai-agent/202602062316305.png) |

| 健身排行榜 | 健身知识库 |
|:---:|:---:|
| ![健身排行榜](https://raw.githubusercontent.com/lmqvq/Upload-image/main/mq-ai-agent/202602062336041.png) | ![健身知识库](https://raw.githubusercontent.com/lmqvq/Upload-image/main/mq-ai-agent/202602062316651.png) |

</details>

---

## 🏗️ 系统架构

### 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        客户端层 (Client)                         │
│            Vue 3 + Arco Design + ECharts + Pinia                │
├─────────────────────────────────────────────────────────────────┤
│                     网关层 (Gateway Layer)                       │
│         Spring Boot Web · 认证鉴权 · CORS · API 限流             │
├──────────┬──────────────┬──────────────┬────────────────────────┤
│ AI 控制器 │ 用户控制器    │ 健身控制器    │ 知识库控制器            │
├──────────┴──────────────┴──────────────┴────────────────────────┤
│                    业务服务层 (Service Layer)                     │
│      KeepApp · MqManus · UserService · FitnessService ...       │
├─────────────────────────────────────────────────────────────────┤
│                   智能体架构层 (Agent Layer)                      │
│     BaseAgent → ReActAgent → ToolCallAgent → MqManus            │
│     Think(推理) → Act(行动) → Observe(观察) 循环                  │
├────────────┬────────────┬───────────────┬───────────────────────┤
│  Spring AI │ RAG 知识库  │  对话记忆管理   │  工具集 (7 种)         │
│  ChatClient│ 向量检索    │  MySQL+Redis  │  搜索/文件/PDF...      │
├────────────┴────────────┴───────────────┴───────────────────────┤
│                     数据存储层 (Data Layer)                       │
│           MySQL 8.0 · Redis · 腾讯云 COS · 向量存储               │
└─────────────────────────────────────────────────────────────────┘
```

### 架构图

![系统架构图](https://mq-picture-1324656182.cos.ap-guangzhou.myqcloud.com/img/202508101625250.png)

### AI 对话系统数据流

![AI对话系统数据流图](https://mq-picture-1324656182.cos.ap-guangzhou.myqcloud.com/img/202508101552167.png)

### ReAct 智能体工作流

```
用户请求 ──→ Think(思考) ──→ 需要工具? ──Yes──→ Act(执行工具) ──→ Observe(观察结果) ──→ 继续循环
                │                                                                         │
                └── No ──→ 直接生成回答 ──→ 返回结果 ←─────────── 任务完成 ←───────────────┘
```

---

## 🛠️ 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| **Java** | 21 | 开发语言 |
| **Spring Boot** | 3.4.6 | 应用框架 |
| **Spring AI** | 1.0.0-M6.1 | AI 应用开发框架 |
| **MySQL** | 8.0+ | 关系型数据库 |
| **MyBatis-Plus** | 3.5.12 | ORM 框架 |
| **Redis** | 6.0+ | 缓存（可选） |
| **通义千问 DashScope** | - | 默认 AI 模型 |
| **Spring AI OpenAI** | 1.0.0-M6 | OpenAI 兼容模型接入 |
| **腾讯云 COS** | - | 对象存储（头像等） |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| **Vue** | 3.2.13 | 前端框架 (Composition API) |
| **Arco Design Vue** | 2.57.0 | UI 组件库 |
| **Vue Router** | 4.5.1 | 路由管理 |
| **Pinia** | 3.0.3 | 状态管理 |
| **Axios** | 1.10.0 | HTTP 客户端 |
| **ECharts** | 6.0.0 | 数据可视化 |
| **Mermaid** | 11.9.0 | 图表渲染 |
| **SCSS** | - | CSS 预处理器 |

### AI 能力

| 能力 | 实现方案 |
|------|----------|
| 对话模型 | 通义千问 / DeepSeek / GLM / Gemini（可配置） |
| RAG 知识库 | 阿里云知识库服务 + 本地 SimpleVectorStore |
| 工具调用 | Spring AI Function Calling + 自定义工具 |
| 对话记忆 | DatabaseChatMemory + Redis 缓存 |
| 流式输出 | SSE (Server-Sent Events) + Reactor |
| 实时进度展示 | 结构化 SSE 事件推送（8 种事件类型） |
| 智能体架构 | ReAct (Reasoning + Acting) 模式 |

---

## 📁 项目结构

```
mq-ai-agent/
├── 📂 src/main/java/com/mq/mqaiagent/
│   ├── 📂 agent/                  # 🤖 智能体核心架构
│   │   ├── BaseAgent.java         #    基础智能体（状态机 + 执行循环 + SSE 事件）
│   │   ├── ReActAgent.java        #    ReAct 模式（Think-Act-Observe）
│   │   ├── ToolCallAgent.java     #    工具调用智能体（实时进度推送）
│   │   └── MqManus.java           #    多功能智能体实例
│   ├── 📂 app/                    # 💪 健身应用
│   │   └── KeepApp.java           #    AI 健身教练（对话/RAG/缓存/流式）
│   ├── 📂 assessment/             # 🧪 标准化评测模块
│   │   ├── controller/            #    评测接口（scheme / profile / record / report）
│   │   ├── service/               #    评测业务编排
│   │   ├── engine/                #    规则评分引擎
│   │   ├── mapper/                #    评测数据访问层
│   │   └── model/                 #    entity / dto / vo
│   ├── 📂 tools/                  # 🔧 工具集（7 种）
│   │   ├── FileOperationTool.java
│   │   ├── WebSearchTool.java
│   │   ├── WebCrawlingTool.java
│   │   ├── ResourceDownloadTool.java
│   │   ├── PDFGenerationTool.java
│   │   ├── GoogleWebSearchTool.java
│   │   └── TerminateTool.java
│   ├── 📂 rag/                    # 📚 RAG 知识库配置
│   ├── 📂 chatmemory/             # 🧠 对话记忆（MySQL + Redis）
│   ├── 📂 ai/                     # 🎛️ 多模型路由与配置
│   ├── 📂 advisor/                # 📋 Spring AI Advisor（日志/敏感词/Re-Reading）
│   ├── 📂 model/                  # 📦 数据模型
│   │   ├── dto/AgentSseEvent.java #    SSE 事件 DTO
│   │   └── enums/SseEventType.java#    SSE 事件类型枚举
│   ├── 📂 controller/             # 🌐 API 控制器
│   ├── 📂 service/                # ⚙️ 业务服务
│   ├── 📂 pool/                   # 🏊 ChatClient 对象池
│   └── 📂 config/                 # ⚙️ 配置类
├── 📂 mq-ai-agent-frontend/       # 🎨 Vue 3 前端项目
│   ├── 📂 src/views/              #    页面组件（含 assessment 页面）
│   │   ├── AssessmentEntry.vue    #    体测数据录入 / 编辑
│   │   └── AssessmentReport.vue   #    体测报告 / 记录列表
│   ├── 📂 src/components/         #    公共组件
│   │   └── ToolCallCard.vue       #    工具调用卡片组件
│   ├── 📂 src/services/           #    API 服务
│   │   └── sseParser.js           #    SSE 消息解析器
│   └── 📂 src/stores/             #    状态管理
├── 📂 sql/                        # 🗃️ 数据库脚本
│   ├── init_all.sql               #    完整初始化脚本（推荐）
│   ├── create_table.sql           #    基础业务表
│   ├── assessment_tables.sql      #    assessment 模块表结构
│   ├── assessment_university_standard_seed.sql   # assessment 方案与项目种子
│   ├── assessment_university_standard_rules.sql  # assessment 评分规则
│   ├── fitness_knowledge_tables.sql #  健身知识表
│   └── knowledge_init_data.sql    #    知识库测试数据
├── 📂 docs/                       # 📄 开发文档
└── 📂 scripts/                    # 📜 部署脚本
```

---

## 🚀 快速开始

### 环境要求

| 环境 | 版本要求 | 必须 |
|------|---------|------|
| Java | 21+ | ✅ |
| Maven | 3.8+ | ✅ |
| Node.js | 14+ | ✅ |
| MySQL | 8.0+ | ✅ |
| Redis | 6.0+ | ❌（可选） |

### 1. 克隆项目

```bash
git clone https://github.com/lmqvq/mq-ai-agent.git
cd mq-ai-agent
```

### 2. 后端启动

```bash
# 2.1 初始化数据库（一键初始化所有表结构和测试数据）
mysql -u root -p < sql/init_all.sql

# 2.2 配置 application.yml
# 修改数据库连接、AI 模型 API Key 等配置（见下方配置说明）

# 2.3 启动后端
mvn spring-boot:run
```

> 📦 **数据库说明**：
> - `sql/init_all.sql` 现在已经完整包含基础业务表、知识库表，以及 `assessment` 模块的表结构、方案种子和评分规则
> - 默认情况下，其他开发者只需要执行这一份 `init_all.sql`，就可以完成数据库初始化
> - `assessment_tables.sql`、`assessment_university_standard_seed.sql`、`assessment_university_standard_rules.sql` 仍然保留在仓库中，便于独立维护和调试

### 2.4 初始化成功校验 SQL

导入完成后，可以执行下面这组 SQL 快速确认 assessment 数据是否完整：

```sql
use mq_ai_agent;

select count(*) as assessmentSchemeCount
from assessment_scheme
where schemeCode = 'CN_UNIVERSITY_PHYSICAL_HEALTH_STANDARD'
  and isDelete = 0;

select count(*) as assessmentSchemeItemCount
from assessment_scheme_item
where schemeCode = 'CN_UNIVERSITY_PHYSICAL_HEALTH_STANDARD'
  and isDelete = 0;

select count(*) as assessmentRuleCount
from assessment_rule
where schemeCode = 'CN_UNIVERSITY_PHYSICAL_HEALTH_STANDARD'
  and ruleVersion = 'v1'
  and isDelete = 0;
```

预期结果：

- `assessmentSchemeCount = 1`
- `assessmentSchemeItemCount = 7`
- `assessmentRuleCount = 582`

### 3. 前端启动

```bash
cd mq-ai-agent-frontend

# 3.1 安装依赖
npm install

# 3.2 启动开发服务器
npm run serve
```

### 4. 访问应用

- 🌐 前端页面：http://localhost:8080
- 📡 后端 API：http://localhost:8123/api/doc.html#/home

### ⚙️ 核心配置

在 `src/main/resources/application.yml` 中配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mq_ai_agent
    username: root
    password: your_password
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}    # 阿里云通义千问 API Key

# 多模型配置（可选）
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

# 搜索 API（超级智能体使用）
search-api:
  api-key: ${SEARCH_API_KEY}

# 腾讯云 COS（头像上传使用）
cos:
  client:
    accessKey: ${COS_ACCESS_KEY}
    secretKey: ${COS_SECRET_KEY}
    region: ${COS_REGION}
    bucket: ${COS_BUCKET}
```

> 💡 建议使用环境变量管理敏感信息，避免硬编码 API Key。

---

## 🤖 智能体架构详解

### 四层继承结构

```
BaseAgent（基础智能体）
  ├── 状态管理：IDLE → RUNNING → FINISHED/ERROR
  ├── 执行循环：最大步数控制，防止无限循环
  ├── 同步 / 流式两种执行模式
  └── SSE 事件推送（8 种事件类型）
       │
       ▼
ReActAgent（ReAct 模式）
  ├── think()：推理阶段 —— 分析任务，决定是否需要行动
  └── act()：行动阶段 —— 执行具体操作，返回结果
       │
       ▼
ToolCallAgent（工具调用）
  ├── 工具管理器（ToolCallingManager）
  ├── 工具自动发现与注册
  ├── 工具执行结果反馈
  └── 实时工具状态推送（thinking/tool_start/tool_complete/tool_error）
       │
       ▼
MqManus（多功能智能体实例）
  ├── 集成 7 种工具
  ├── 对话记忆 + ChatClient 池化
  ├── 系统提示词定制
  └── 工具摘要生成（可读的工具执行描述）
```

### 内置工具一览

| 工具 | 功能 | 应用场景 | 实时状态 |
|------|------|----------|----------|
| 📄 FileOperationTool | 文件创建 / 读取 / 写入 / 删除 | 保存健身计划、训练记录 | ✅ |
| 🔍 WebSearchTool | 网络信息搜索 | 搜索最新健身资讯 | ✅ |
| 🌐 WebCrawlingTool | 网页内容抓取（Jsoup） | 获取健身文章、营养信息 | ✅ |
| ⬇️ ResourceDownloadTool | 资源文件下载 | 下载健身动作示意图 | ✅ |
| 📑 PDFGenerationTool | PDF 文档生成（iText） | 生成健身计划 PDF 报告 | ✅ |
| 🔎 GoogleWebSearchTool | Google 搜索 (SerpApi) | 高质量搜索结果 | ✅ |
| 🛑 TerminateTool | 终止智能体循环 | 任务完成信号 | ✅ |

> ✅ 所有工具均支持实时状态推送，用户可在前端看到工具执行进度和结果摘要

---

## 🚢 部署指南

### Docker Compose 一键部署

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

### Nginx 反向代理

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

## 🤝 贡献指南

我们非常欢迎各种形式的贡献！请查看 [CONTRIBUTING.md](CONTRIBUTING.md) 了解详情。

```bash
# 1. Fork & Clone
git clone https://github.com/your-username/mq-ai-agent.git

# 2. 创建功能分支
git checkout -b feature/your-feature

# 3. 提交更改（遵循 Conventional Commits）
git commit -m "feat: add your feature"

# 4. 推送并创建 PR
git push origin feature/your-feature
```

---

## 📄 开源协议

本项目基于 [MIT License](LICENSE) 开源，你可以自由使用、修改和分发。

---

## 🙏 致谢

- [Spring AI](https://spring.io/projects/spring-ai) - AI 应用开发框架
- [阿里云通义千问](https://dashscope.aliyun.com/) - 大语言模型服务
- [Arco Design](https://arco.design/) - UI 组件库
- [Vue.js](https://vuejs.org/) - 前端框架
- [DeepSeek](https://www.deepseek.com/) - AI 模型

---

## 📞 联系方式

- **作者**：LMQICU
- **邮箱**：lmqicu@qq.com
- **GitHub**：[https://github.com/lmqvq](https://github.com/lmqvq)
- **在线体验**：[https://cozi.chat/](https://cozi.chat/)

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给一个 Star 支持一下！⭐**

Made with ❤️ by [LMQICU](https://github.com/lmqvq)

[回到顶部](#-mq-ai-agent)

</div>
