# AI Coze Agent 模块设计

## 概述

实现腾跃校园健身平台的 AI 智能模块，通过 Coze Agent 编排引擎提供计划生成、数据分析、食谱推荐、动作指导、伤病恢复五大 AI 功能。SSE 流式响应 + 打字机效果。

## 技术选型

| 层 | 选择 | 理由 |
|:---|:---|:---|
| 后端 SSE | Spring `SseEmitter` | 同步模型，无需 WebFlux，spring-boot-starter-web 内置 |
| 调用 Coze | `RestTemplate` + `ResponseExtractor` 逐行读 SSE | 同步流式消费 |
| 10 分钟缓存 | JVM `ConcurrentHashMap` + 时间戳 | 单实例够用，无外部依赖 |
| Session 存储 | Redis `String` 24h TTL | 按功能类型独立 key |
| 每日限额 | `AiCallLogService`（DB）+ Redis 缓存 | 双重保障，DB 持久化 + Redis 快速计数 |
| 前端 SSE | `fetch` + `ReadableStream` | 原生支持，可 AbortController 取消 |

## API 设计

### SSE 端点（各功能独立）

| 方法 | 路径 | 说明 | 请求体 |
|:---|:---|:---|:---|
| POST | `/api/ai/chat` | 通用对话 | `{ message }` |
| POST | `/api/ai/plan` | 生成健身计划 | `{ message }` |
| POST | `/api/ai/analyze` | 数据分析 | `{ message }` |
| POST | `/api/ai/recipe` | 食谱推荐 | `{ message }` |
| POST | `/api/ai/exercise-guide` | 动作指导 | `{ message }` |
| GET | `/api/ai/quota` | 查询剩余额度 | - |

响应格式: `text/event-stream`，SSE 协议 `data: {JSON}\n\n`

### SSE 事件类型

- `chunk` — 文本片段（前端打字机渲染）
- `done` — 流结束
- `error` — 错误信息

## 后端文件

### 新建

| 文件 | 职责 |
|:---|:---|
| `coze/CozeClientImpl.java` | 实现 CozeClient 接口。HTTP POST 到 Coze `stream_run` API，解析 SSE 响应行，逐条回调 CozeSseEmitter |
| `coze/CozeSseEmitterImpl.java` | 实现 CozeSseEmitter 接口。封装 Spring SseEmitter，管理超时（30s）和完成回调 |
| `coze/ContextBuilderImpl.java` | 实现 ContextBuilder 接口。查询 User + CheckIn 数据，组装身体数据（身高/体重/目标/等级）+ 近 7 日运动摘要文本 |
| `controller/AiController.java` | 6 个端点。校验登录 + 身体数据完整性 → 调用 AiService |
| `service/AiService.java` | 编排层：限额检查 → 缓存匹配 → 构建上下文 → 调用 CozeClient → 记录日志。处理超时降级和兜底文案 |
| `config/AsyncConfig.java` | SseEmitter 线程池配置 |
| `dto/AiRequest.java` | `{ message: String }` |

### 已存在

- `coze/CozeConfig.java` — 配置类，读取 `coze.*` 属性 ✅
- `coze/CozeClient.java` — 接口，定义 `streamRun(prompt, sessionId, emitter)` ✅
- `coze/CozeSseEmitter.java` — 接口，定义 `send/complete/error/isCompleted` ✅
- `coze/ContextBuilder.java` — 接口，定义 `buildContext(userId)` ✅
- `service/AiCallLogService.java` — 每日限额检查 + 日志记录 ✅
- `entity/AiCallLog.java` + `mapper/AiCallLogMapper.java` ✅

## 业务流程

```
POST /api/ai/plan { message }
  → Auth 校验 JWT Token
  → 检查用户是否完善身体数据（否则 400）
  → AiService.streamRun(userId, callType, message)
      → 限额检查: countTodayCalls >= 10 → 429
      → 缓存检查: md5(userId+callType+message) 命中 → 直接返回缓存 SSE
      → ContextBuilder.buildContext(userId) 组装前缀
      → CozeClient.streamRun(fullPrompt, sessionId, emitter)
          → POST coze_api/stream_run { prompt, session_id, project_id }
          → 逐行读取 SSE 响应
          → emitter.send(chunk) 转发
      → 完成 → AiCallLogService.log() 记录
      → 异常/超时 → emitter.error() 返回兜底文案
  → SseEmitter → 前端 ReadableStream → 打字机渲染
```

## 关键规则

1. 每日上限 10 次，超出返回 `{ code: 429, message: "今日AI调用次数已用完" }`
2. 30 秒超时，返回兜底文案（每种功能预置一段通用建议）
3. 相同参数 10 分钟内返回缓存（`ConcurrentHashMap`，定时清理过期项）
4. 未完善身体数据不可使用 AI（`fitnessGoal == null || fitnessLevel == null`）
5. session_id: Redis key `coze:session:{userId}:{callType}`，24h TTL
6. 每日限额计数: Redis key `coze:daily:{userId}:{date}`，当天零点过期

## 前端设计

**AiPage.vue** — 功能卡片 + 对话流：

- 顶部：5 个功能快捷卡片（点击切换 callType，显示对应 icon/描述）
- 中间：对话流区域（消息气泡，AI 消息打字机逐字渲染）
- 底部：输入框 + 发送按钮 + 剩余额度显示
- 右侧/底部：用户上下文卡片（身体数据摘要）

打字机效果：`fetch + ReadableStream` 逐 chunk 追加文本，`requestAnimationFrame` 平滑渲染。

**api/ai.ts** — 封装各功能 SSE 请求，返回 AbortController 用于取消。
