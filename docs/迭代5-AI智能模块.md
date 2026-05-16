# 迭代5：AI 智能模块（Coze Agent）

## 后端任务

| 接口 | 方法 | 路径 | 说明 | 响应 |
|:---|:---|:---|:---|:---|
| AI 对话 | POST | `/api/ai/chat` | 通用入口，Coze 意图识别自动路由 | SSE |
| 生成计划 | POST | `/api/ai/plan` | AI 生成健身计划 | SSE |
| 数据分析 | POST | `/api/ai/analyze` | AI 分析运动数据 | SSE |
| 食谱推荐 | POST | `/api/ai/recipe` | AI 推荐食谱 | SSE |
| 动作指导 | POST | `/api/ai/exercise-guide` | AI 动作指导 | SSE |
| 查询额度 | GET | `/api/ai/quota` | 当日剩余调用次数 | JSON |

## Coze 集成实现

### 文件
- `coze/CozeConfig.java` — 已存在，从 .env 读取配置
- `coze/CozeClientImpl.java` — 实现 CozeClient 接口，HTTP POST 到 `stream_run`，解析 SSE 响应
- `coze/CozeSseEmitterImpl.java` — 实现 CozeSseEmitter，封装 Spring SseEmitter
- `coze/ContextBuilderImpl.java` — 实现 ContextBuilder，查询用户身体数据 + 近 7 日运动摘要，拼接上下文注入 prompt
- `service/AiService.java` — AI 业务服务，调用限流 + 缓存 + 日志记录
- `controller/AiController.java` — 5 个 SSE 接口 + 额度查询

### 业务规则
- 每人每日 AI 调用上限 10 次（Redis 计数器，每日零点重置）
- Coze API 调用 30 秒超时，超时返回兜底文案
- 相同参数 10 分钟内返回缓存结果
- 用户注册后需完善身体数据才能使用 AI 功能
- session_id 存储在 Redis 中，24 小时过期
- AI 调用记录写入 `ai_call_log` 表

## 数据表
- `ai_call_log` — AI 调用日志表（已存在）

## 前端任务
- AI 助手页面 — 对话式交互 + 5 个快捷功能卡片
- SSE 客户端 — EventSource / fetch + ReadableStream 打字机渲染
- 额度展示 — 顶部显示当日剩余次数
- 上下文提示 — 对话区常驻用户身体数据摘要
- Axios API 模块（`api/ai.ts`）

## 页面清单
- `/ai` — AI 助手

## 测试类型
- 后端单元测试 + 接口测试 + Coze 集成测试（Mock Coze API、超时降级、限流验证）
