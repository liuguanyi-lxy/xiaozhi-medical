# 小智医疗 —— AI 智慧医疗预约挂号平台

> 完整系统仓库：LangChain4j AI 智能客服 + 医院预约挂号微服务平台 + 医院模拟系统 + 三端前端。
> AI 模块独立介绍见 [`java-ai-langchain4j/README.md`](./java-ai-langchain4j/README.md)。

## 系统架构

```
                    ┌─────────────────────────┐
   用户自然语言 ──▶ │  AI 智能客服 (8081)       │◀──┐
                    │  LangChain4j Agent       │   │
                    │  RAG(Milvus) + 工具调用    │   │
                    └──────────┬──────────────┘   │
                               │ HTTP(号源查询/预约) │
                               ▼                  │
┌──────────┐   ┌───────────────────────────────┐  │
│ 医院模拟端 │──▶│  平台微服务（Spring Cloud Alibaba）│──┘
│ hospital- │   │  Gateway(8222) 统一 JWT 鉴权    │
│ manage    │   │  service-hosp / orders / user  │
└──────────┘   │  msm / cmn / oss / statistics  │
               └───────┬───────────┬────────────┘
                       ▼           ▼
                MySQL/MongoDB/  RabbitMQ
                Redis/Nacos     (订单/短信/提醒)
                       ▲           ▲
               ┌───────┴───────────┴───────┐
               │ 前端三端：用户端(Nuxt SSR)   │
               │ 管理端(Vue2) 小智UI(Vue3)   │
               └───────────────────────────┘
```

## 目录结构

| 目录 | 内容 | 技术栈 |
|---|---|---|
| [`java-ai-langchain4j/`](./java-ai-langchain4j/) | **AI 智能客服**：RAG 检索增强（本地 Milvus + 幂等入库）、Function Calling 号源查询/预约挂号、多轮会话记忆、流式输出 | Spring Boot 3 · LangChain4j 1.4 · 通义千问/DeepSeek · Milvus · MongoDB |
| [`后端代码/平台系统后端代码/yygh_parent/`](./后端代码/平台系统后端代码/yygh_parent/yygh_parent/) | **医院预约平台**：8 个微服务（医院/订单/用户/短信/字典/存储/统计/任务）+ Gateway JWT 统一鉴权 + Feign 降级 + MQ 解耦 | Spring Boot 3 · Spring Cloud Alibaba · MySQL · Redis · RabbitMQ · MongoDB |
| [`后端代码/医院模拟系统后端代码/`](./后端代码/医院模拟系统后端代码/) | **医院模拟系统**：医院/科室/排班数据对接，签名校验 | Spring Boot 2 · MyBatis-Plus |
| [`前端代码/用户操作端前端代码/`](./前端代码/用户操作端前端代码/) | **用户操作端**：首页/医院/挂号/订单/就诊人，SSR 利于 SEO | Nuxt 2 · Element UI |
| [`前端代码/平台管理端前端代码/`](./前端代码/平台管理端前端代码/) | **平台管理端**：医院设置/排班/用户审批/预约统计 | Vue 2 · Element UI · vue-admin-template |
| [`前端代码/xiaozhi-ui/`](./前端代码/xiaozhi-ui/) | **小智聊天 UI**：流式打字机渲染、HTML 转义防 XSS | Vue 3 · Vite · Element Plus |
| [`资料/`](./资料/) | 数据库脚本（5 库）、示例数据、RAG 知识库文档 | — |

## 核心链路：对话式预约挂号

1. 用户在小智 UI 用自然语言提问（"明天上午神经内科有号吗"）
2. LangChain4j Agent 召回 RAG 知识片段（Milvus 向量检索）+ 选择工具
3. `querySchedule` 工具调用平台真实排班接口，按 `availableNumber > 0 && status == 1` 校验，返回真实医生/职称/剩余号数
4. 用户确认后 `bookAppointment` 落库，防幻觉（id 置空 + 落库前复查）+ 防重复预约
5. 回复经 WebFlux `Flux` 流式推送，前端打字机渲染

## 快速开始

各模块独立启动，最小化体验 AI 客服：

```bash
# 1. 准备依赖服务：MySQL、MongoDB、Milvus(19530)、医院号源系统(8201)
# 2. 配置环境变量（见 java-ai-langchain4j/README.md）
cd java-ai-langchain4j
mvn spring-boot:run       # 8081 端口，接口文档 /doc.html

# 平台微服务（需要 Nacos/RabbitMQ）
cd 后端代码/平台系统后端代码/yygh_parent/yygh_parent
# 按模块分别启动 service_gateway、service_hosp、service_orders、service_user ...
```

## 免责声明

本项目为个人学习用途。AI 模块仅提供导诊与挂号流程辅助，不构成医疗建议；提示词与业务边界的设计说明见 AI 模块 README。
