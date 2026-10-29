# 小智医疗 —— 基于 LangChain4j 的 RAG 智能问诊系统

> 面向医疗场景的 AI 对话服务。基于 LangChain4j 构建 Agent，结合 **RAG 检索增强** 与 **Function Calling**，支持多轮问诊、科室导诊与预约挂号。

## 核心能力

| 能力 | 实现方式 |
|---|---|
| **RAG 检索增强** | 医院/科室/神经内科知识文档向量化后存入 Pinecone，对话时按语义检索相关片段注入上下文，规避大模型幻觉 |
| **Function Calling** | `AppointmentTools` 暴露号源查询、预约挂号等工具方法，由模型自主判断何时调用 |
| **多轮会话记忆** | 按 `memoryId` 隔离会话，记忆持久化到 MongoDB（重启不丢失），而非进程内存 |
| **流式输出** | 基于 WebFlux + Reactor `Flux` 逐字返回，前端可边生成边渲染 |
| **双模型接入** | DeepSeek（OpenAI 兼容协议）负责通用对话，通义千问 `qwen-plus` 负责流式问诊 |

## 技术栈

| 分类 | 选型 |
|---|---|
| 基础框架 | JDK 17 · Spring Boot 3 · Spring WebFlux |
| AI 框架 | **LangChain4j 1.4.0-beta10** |
| 大模型 | DeepSeek（`deepseek-chat`）· 通义千问 `qwen-max` / `qwen-plus`（流式） |
| 向量化 | 阿里百炼 `text-embedding-v2` |
| 向量库 | **Pinecone**（云托管，index `xiaozhiyiliao`） |
| 会话记忆 | MongoDB（`chat_memory_db`） |
| 业务数据 | MySQL 8.0 · MyBatis-Plus（预约挂号） |
| API 文档 | Knife4j（OpenAPI 3） |

## 请求链路

```
用户提问
   │
   ▼
POST /xiaozhi/chat ──▶ XiaozhiController
                          │
                          ▼
                    XiaozhiAgent（@AiService）
                    ├── SystemMessage：xiaozhi-prompt-template.txt 定义人设与边界
                    ├── ChatMemory：按 memoryId 从 MongoDB 读取历史
                    ├── ContentRetriever：向量检索 Pinecone → 注入相关知识
                    ├── Tools：模型判断是否需要查号源 / 下单预约
                    └── StreamingChatModel：qwen-plus 流式生成
                          │
                          ▼
                    Flux<String> 逐字返回
```

## 关键设计

### 1. RAG 知识库构建

- `KnowledgeBaseIngestor` 扫描 `classpath:knowledge/*.md`，切分后调用 `text-embedding-v2` 向量化，写入 Pinecone 指定 namespace
- 换知识库只需替换文档或改 `rag.knowledge-location` 配置，不用改代码
- 检索结果作为上下文拼进 prompt，让模型基于真实医院信息回答而非编造

### 2. Function Calling 打通业务系统

- `AppointmentTools` 声明工具方法，LangChain4j 自动生成 function schema 交给模型
- 模型根据用户意图自主决定是否调用（例如用户说"帮我挂神经内科周三的号"）
- 工具内部通过 `HttpUtils` 调用医院号源系统（`hospital.api.base-url`），拿到真实号源

### 3. 会话记忆持久化

- 自定义 `MongoChatMemoryStore` 实现 `ChatMemoryStore` 接口
- 每条消息按 `memoryId` 落到 MongoDB，服务重启后历史仍在
- 不同用户 / 不同会话互不串扰

### 4. AiService 显式布线

```java
@AiService(
    wiringMode = EXPLICIT,
    streamingChatModel = "qwenStreamingChatModel",
    chatMemoryProvider = "chatMemoryProviderXiaozhi",
    tools = "appointmentTools",
    contentRetriever = "contentRetrieverXiaozhiPinecone"
)
public interface XiaozhiAgent {
    @SystemMessage(fromResource = "xiaozhi-prompt-template.txt")
    Flux<String> chat(@MemoryId Long memoryId, @UserMessage String userMessage);
}
```

`EXPLICIT` 模式按 Bean 名精确解析，依赖拼写错误会在启动时直接暴露，而不是运行时静默降级。

## 快速开始

### 1. 配置环境变量

项目所有密钥均通过环境变量注入，**代码库中不含任何明文凭据**：

```bash
DEEPSEEK_API_KEY=        # DeepSeek 密钥
DASHSCOPE_API_KEY=       # 阿里百炼密钥（对话 / 向量 / 图像共用）
PINECONE_API_KEY=        # Pinecone 密钥
MYSQL_HOST=127.0.0.1
MYSQL_PORT=3306
MYSQL_USERNAME=root
MYSQL_PASSWORD=
MONGODB_HOST=127.0.0.1
MONGODB_PORT=27017
HOSPITAL_API_BASE_URL=http://localhost:8201
```

### 2. 准备依赖服务

| 服务 | 用途 | 默认端口 |
|---|---|---|
| MySQL | 预约挂号数据 | 3306 |
| MongoDB | 对话记忆 | 27017 |
| Pinecone | 向量检索 | 云服务 |
| 医院号源系统 | 号源查询 / 预约 | 8201 |

### 3. 启动

```bash
mvn spring-boot:run
```

服务端口 `8081`，接口文档：`http://localhost:8081/doc.html`

### 4. 调用示例

```bash
curl -X POST http://localhost:8081/xiaozhi/chat \
  -H "Content-Type: application/json" \
  -d '{"memoryId": 1, "message": "神经内科有哪些医生？周三有号吗？"}'
```

## 目录结构

```
src/main/java/com/atguigu/java/ai/langchain4j
├── Assistant
│   ├── XiaozhiAgent.java        # @AiService 声明的对话接口
│   └── Assistant.java
├── config
│   ├── XiaozhiAgentConfig.java  # 流式模型、记忆提供者、检索器装配
│   ├── EmbeddingStoreConfig.java# Pinecone 向量库配置
│   └── MongoConfig.java
├── controller                   # /xiaozhi/chat 流式接口
├── store
│   ├── KnowledgeBaseIngestor.java  # 知识文档向量化入库
│   ├── KnowledgeFileRecord.java
│   └── MongoChatMemoryStore.java   # MongoDB 会话记忆
├── tools
│   └── AppointmentTools.java    # Function Calling 工具
├── service / mapper / entity    # 预约挂号业务（MySQL）
└── utils / bean                 # 工具类与请求响应体
```
