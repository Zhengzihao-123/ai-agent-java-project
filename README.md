# AI Agent System

基于 Spring Boot 3.x 构建的课岗证赛一体化智能体系统，集成 AI 大模型、知识库 RAG、会话管理和 RBAC 权限系统。

## 功能特性

### 核心功能
- **AI 智能聊天** - 支持4种智能体角色（课程、培训、考证、竞赛）
- **知识库 RAG** - 文档分块检索、基于知识库回答、引用来源标注
- **会话管理** - 多会话支持、会话重命名、历史记录管理
- **权限系统** - RBAC 用户角色权限管理

### 技术亮点
- 密码加密（BCrypt）
- AI 流式输出（SSE）
- 多格式文档解析（DOCX/PDF/TXT/MD）
- 批量导入知识库

## 技术栈

| 分类 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.1.10 |
| ORM | MyBatis-Plus | 3.5.5 |
| 数据库 | MySQL | 8.0+ |
| 安全 | Spring Security | 6.x |
| AI API | ChatAnywhere GPT-4o-mini | - |
| 文档解析 | Apache POI / PDFBox | - |

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Maven 3.6+

### 数据库配置

创建数据库并执行初始化脚本：

```sql
CREATE DATABASE ai_agent_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_agent_db;
source src/main/resources/init.sql;
```

### 配置文件

修改 `application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_agent_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password

openai:
  api-key: your_api_key
  base-url: https://api.chatanywhere.tech/v1
```

### 启动项目

```bash
cd ai-agent-system
mvn spring-boot:run
```

访问 http://localhost:8080

## API 接口

### 用户接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/user/register` | POST | 用户注册 |
| `/api/user/login` | POST | 用户登录 |

### 聊天接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/chat/send` | POST | 发送消息（非流式） |
| `/api/chat/send-with-rag` | POST | 发送消息（基于知识库） |
| `/api/chat/stream` | POST | 发送消息（SSE流式） |
| `/api/chat/history/{userId}` | GET | 获取聊天历史 |
| `/api/chat/conversation/{userId}/{conversationId}` | DELETE | 删除会话 |

### 知识库接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/knowledge/upload` | POST | 上传文档 |
| `/api/knowledge/list/{userId}` | GET | 获取文档列表 |
| `/api/knowledge/search` | GET | 搜索知识库 |
| `/api/knowledge/batch-import` | POST | 批量导入文档 |
| `/api/knowledge/{userId}/{docId}` | DELETE | 删除文档 |

## 项目结构

```
ai-agent-system/
├── src/main/java/com/example/aiagent/
│   ├── controller/          # REST API 控制层
│   ├── service/             # 业务逻辑层
│   ├── repository/          # 数据访问层
│   ├── entity/              # 数据库实体
│   ├── dto/                 # 数据传输对象
│   ├── config/              # 配置类
│   ├── common/              # 公共工具
│   └── util/                # 工具类
├── src/main/resources/
│   ├── application.yml      # 应用配置
│   ├── init.sql             # 数据库初始化脚本
│   └── static/              # 前端静态资源
└── pom.xml                  # Maven 依赖管理
```

## 使用说明

### 注册登录

```javascript
// 注册
fetch('/api/user/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'user', password: '123456' })
});

// 登录
fetch('/api/user/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'user', password: '123456' })
});
```

### 发送消息（RAG模式）

```javascript
fetch('/api/chat/send-with-rag', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        userId: 1,
        conversationId: 'conv_001',
        agentRole: 'course',
        message: '什么是 Servlet？'
    })
}).then(r => r.json()).then(d => {
    console.log('回答:', d.data.answer);
    console.log('是否使用知识库:', d.data.hasKnowledge);
    console.log('引用来源:', d.data.sources);
});
```

### 批量导入知识库

```javascript
fetch('/api/knowledge/batch-import?userId=1&directory=C:\\path\\to\\knowledge', {
    method: 'POST'
});
```

## 智能体角色

| 角色 | 值 | 描述 |
|------|------|------|
| 课程智能体 | course | 解答学科课程问题 |
| 培训智能体 | training | 解答职业技能培训问题 |
| 考证智能体 | cert | 解答职业资格考试问题 |
| 竞赛智能体 | competition | 解答学科竞赛问题 |

## 开发指南

### 添加新智能体角色

1. 在 `ChatHistoryServiceImpl.getSystemPrompt()` 方法中添加角色
2. 在前端 `script.js` 的 `agentMap` 对象中添加映射

### 添加新 API 接口

1. 创建 Controller 类
2. 创建 Service 接口和实现类
3. 创建 Mapper 接口

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！

---

*项目开发中，持续更新中...*
