<div align="center">

# 🏋️ MQ AI Agent Frontend

**智能健身助手前端项目 | Vue 3 + Arco Design + ECharts**

[![Vue](https://img.shields.io/badge/Vue-3.2.13-4FC08D?style=for-the-badge&logo=vuedotjs&logoColor=white)](https://vuejs.org/)
[![Arco Design](https://img.shields.io/badge/Arco%20Design-2.57.0-165DFF?style=for-the-badge)](https://arco.design/)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

[🌐 在线体验](https://cozi.chat/) &nbsp;|&nbsp; [📖 主项目](../README.md) &nbsp;|&nbsp; [🐛 问题反馈](https://github.com/lmqvq/mq-ai-agent/issues)

</div>

---

> 💡 这是 [MQ AI Agent](../README.md) 项目的前端部分，完整文档请查看主项目 README。

---

## ✨ 核心功能

| 功能 | 描述 |
|------|------|
| 🤖 **AI 健身教练** | 基于 RAG 知识库的专业健身指导 |
| 💬 **AI 超级智能体** | 支持工具调用的通用 AI 助手 |
| 🔄 **流式对话** | SSE 实时流式响应，类 ChatGPT 体验 |
| 📊 **健身数据** | 体重/体脂率/BMI 追踪 + ECharts 可视化 |
| 🏆 **健身排行榜** | 用户健身成就排名 |
| 📚 **知识库** | 健身知识浏览与管理 |
| 👤 **用户系统** | 注册/登录/个人中心/头像上传 |
| 🌙 **亮暗模式** | 完整支持亮色/暗色主题切换 |

---

## 🛠️ 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| **Vue** | 3.2.13 | Composition API |
| **Arco Design Vue** | 2.57.0 | UI 组件库 |
| **Vue Router** | 4.5.1 | 路由管理 |
| **Pinia** | 3.0.3 | 状态管理 |
| **Axios** | 1.10.0 | HTTP 客户端 |
| **ECharts** | 6.0.0 | 数据可视化 |
| **Mermaid** | 11.9.0 | 图表渲染 |
| **SCSS** | - | 样式预处理 |

---

## 📁 项目结构

```
src/
├── views/                # 页面组件 (11 个页面)
│   ├── Landing.vue       #   着陆页
│   ├── Home.vue          #   主页
│   ├── Login.vue         #   登录页
│   ├── Register.vue      #   注册页
│   ├── FitnessMaster.vue #   AI 健身教练
│   ├── SuperAgent.vue    #   AI 超级智能体
│   ├── FitnessData.vue   #   健身数据
│   ├── FitnessRanking.vue#   健身排行榜
│   ├── FitnessKnowledge.vue # 健身知识库
│   ├── KnowledgeManage.vue  # 知识库管理
│   └── UserProfile.vue   #   个人中心
├── components/           # 公共组件
│   ├── ChatInterface.vue #   聊天界面
│   └── ChatManager.vue   #   对话管理
├── services/             # API 服务
├── stores/               # Pinia 状态管理
├── router/               # 路由配置
└── styles/               # 全局样式
```

---

## 🚀 快速开始

### 环境要求

- **Node.js**: 14+
- **npm**: 6+ 或 **yarn**: 1.22+

### 安装与运行

```bash
# 1. 安装依赖
npm install

# 2. 启动开发服务器
npm run serve

# 3. 访问应用
# http://localhost:8080
```

### 开发命令

```bash
npm run serve   # 开发环境
npm run build   # 生产构建
npm run lint    # 代码检查
```

### API 配置

在 `src/services/api.js` 中配置后端地址：

```javascript
baseURL: 'http://localhost:8123/api'  // 后端 API 地址
```

---

## 📄 许可证

[MIT License](LICENSE)

---

## 🔗 相关链接

- [🏠 主项目](../README.md)
- [🌐 在线体验](https://cozi.chat/)
- [🐛 问题反馈](https://github.com/lmqvq/mq-ai-agent/issues)

---

<div align="center">

Made with ❤️ by [LMQICU](https://github.com/lmqvq)

</div>
