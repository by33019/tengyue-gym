# 腾跃 - 校园健身打卡智能计划助手 - 需求开发文档

**文档版本**：V3.0

**开发工具**：Claude Code

**技术选型**：SpringBoot3 + Vue3 + MySQL8 + Redis + Docker + Coze Agent 智能体编排

## 项目概述

### 项目背景

校园学生缺乏系统化运动规划、实时打卡监督与个性化健身指导，传统健身模式无数据沉淀、无科学计划、无效果反馈。本项目打造轻量化校园健身管理平台，实现**打卡记录、计划定制、数据统计、AI 智能指导**一体化服务，满足实训全流程开发与容器化部署要求。

### 项目目标

1. 完成可演示、可容器化部署的完整 Web 应用
2. 涵盖**3 大业务模块、AI 智能模块、8 张以上业务表**
3. 实现`docker compose up --build`一键启动全系统
4. 代码遵循软件工程规范，完成 Git 版本控制与 AI 协作记录
5. AI 模块采用 **Coze Agent 智能体编排**，通过工作流实现五大功能路由与专业应答

## 用户角色

1. **普通健身用户**：注册登录、健身打卡、计划管理、数据查看、获取 AI 建议
2. **健身督导**：计划审核、打卡数据查看、健身提醒发布
3. **系统管理员**：用户管理、内容审核、系统配置、数据监控

---

## 核心业务模块

### 模块 1：用户认证与个人中心

#### 基础功能

- 用户注册、登录、退出
- 个人信息管理（身高 / 体重 / 健身目标 / 运动基础）
- 账号安全与密码修改
- 个人健身数据概览

#### 补充功能

- **身体数据档案**：记录并可视化体重/体脂变化曲线，作为后续 AI 分析的数据基础
- **运动基础评估**：首次注册时通过简单问卷（运动频率、可完成动作等）自动判定运动等级（入门 / 进阶 / 高级），用于计划生成的强度基准
- **头像上传**：支持头像裁剪与上传，文件存储至后端静态资源目录或对象存储
- **账号注销**：软删除机制，保留 30 天数据恢复期，30 天后自动清理关联数据

### 模块 2：健身打卡与计划管理

#### 基础功能

- 运动打卡（类型 / 时长 / 消耗 / 配图）
- 健身计划创建、编辑、停用
- 计划执行跟踪与打卡日历
- 历史打卡记录查询与导出

#### 补充功能

- **预设运动模板**：系统内置常见运动类型（跑步、力量训练、瑜伽、HIIT、游泳等），每种模板含默认时长与卡路里消耗参考值，用户也可自定义运动类型
- **打卡图片水印**：上传打卡图片自动添加时间戳水印，防止虚假打卡
- **计划模板市场**：督导/管理员可发布经审核的公共健身计划模板，用户一键套用后自动按个人参数调整强度
- **连续打卡奖励**：7 天 / 30 天 / 100 天连续打卡成就徽章，增强用户粘性
- **计划难度自适应**：当计划执行率连续低于 50% 时，系统提示降低计划难度或重新 AI 生成
- **打卡记录导出**：支持导出为 Excel/CSV，方便用户自行二次分析

### 模块 3：数据统计与健身社区

#### 基础功能

- 周 / 月 / 年度运动数据统计
- 健身效果分析报告
- 健身动态发布、点赞、评论
- 督导 / 管理员数据看板

#### 补充功能

- **核心数据指标**：运动总时长、总消耗卡路里、打卡天数、平均每次运动时长、周环比/月同比变化率
- **可视化图表**：使用 ECharts 渲染周运动趋势折线图、运动类型饼图、月度打卡热力图
- **健身排行榜**：周/月打卡天数排行榜与消耗卡路里排行榜，仅展示 Top20，支持匿名开关
- **动态互动**：支持评论回复（二级评论）、@提醒、举报功能
- **督导工作台**：查看所负责用户的打卡率、异常提醒记录、AI 建议历史，可批量发送提醒

---

## AI 智能模块（基于 Coze Agent 智能体编排）

### 架构概述

AI 模块采用 **Coze 平台 Agent 智能体编排** 方案，在 Coze 平台上完成智能体的核心编排工作，后端通过调用 Coze 工作流 API 与智能体交互。相较于直接调用通用大模型 API，Coze 智能体编排具备以下优势：

| 维度 | 直接调用大模型 API | Coze Agent 编排 |
|:---:|:---:|:---:|
| Prompt 管理 | 后端硬编码 / 数据库存储 | Coze 平台可视化编排，热更新无需发版 |
| 多功能路由 | 后端按类型切换 Prompt | 智能体内置意图识别，自动路由到对应工作流节点 |
| 输出格式控制 | 需手动解析 & 校验 | 工作流节点结构化输出，格式更稳定 |
| 知识库 / 插件 | 需自行搭建 | Coze 内置知识库、插件市场直接挂载 |
| 迭代成本 | 改 Prompt 需重新部署 | Coze 平台即时生效 |

### 整体架构图

```
用户请求 → Vue3 前端 → SpringBoot 后端 → gym_coze.py (Coze API 客户端)
                                                    ↓
                                            Coze 工作流 API
                                            (stream_run)
                                                    ↓
                                        ┌─────────────────────────┐
                                        │   Coze 智能体 (Agent)    │
                                        │                         │
                                        │  意图识别 → 功能路由     │
                                        │  ┌───────┐ ┌──────────┐ │
                                        │  │计划生成│ │数据分析  │ │
                                        │  └───────┘ └──────────┘ │
                                        │  ┌───────┐ ┌──────────┐ │
                                        │  │食谱推荐│ │动作指导  │ │
                                        │  └───────┘ └──────────┘ │
                                        │  ┌───────┐              │
                                        │  │伤病恢复│              │
                                        │  └───────┘              │
                                        └─────────────────────────┘
```

### Coze 智能体功能模块

Coze 平台已编排完成的智能体包含以下五大功能模块，通过用户输入自动路由到对应节点：

#### 1. 个性化健身计划生成

依据用户身高、体重、健身目标、可用时间、运动基础，生成每日运动安排与强度建议

- 生成的计划以结构化 JSON 返回，包含每日动作名称、组数/次数、休息时长、注意事项，前端按日历视图渲染
- 若用户已有进行中的计划，AI 生成时需提示"当前计划尚未完成，是否替换"

#### 2. 运动数据智能分析

基于打卡数据与计划执行率，输出效果评估、改进建议、异常提醒

- 分析报告维度包括：计划完成度、卡路里消耗趋势、运动类型多样性评分、连续中断预警
- 报告支持生成文字版摘要供用户分享到社区动态

#### 3. 健身食谱推荐

结合减脂 / 增肌 / 塑形目标，生成一日三餐营养食谱

- 食谱需标注总热量、三大宏量营养素（蛋白质/碳水/脂肪）配比
- 提供替代食材选项（如"鸡胸肉可替换为虾仁"），适配校园食堂场景

#### 4. 运动动作指导

用户输入具体动作名称（如"深蹲"），AI 返回动作要点、常见错误、建议组数与呼吸节奏，降低运动损伤风险

#### 5. 伤病恢复建议

用户标记受伤部位或不适后，AI 给出替代动作建议与恢复期运动方案，避免带伤训练

### Coze API 调用设计

#### 调用方式

后端通过 HTTP 调用 Coze 工作流的 `stream_run` 接口，采用 **SSE（Server-Sent Events）** 流式返回，实现打字机效果的实时响应。

核心调用参数：

| 参数 | 说明 |
|:---:|:---|
| URL | `https://q9khtybwsw.coze.site/stream_run` |
| 认证 | Bearer Token（Coze 平台颁发的 OAuth Access Token） |
| Content-Type | `application/json` |
| Accept | `text/event-stream` |
| project_id | Coze 项目 ID（`7640283069890609190`） |
| session_id | 会话 ID（多轮对话上下文保持） |

#### 请求体结构

```json
{
  "content": {
    "query": {
      "prompt": [
        {
          "type": "text",
          "content": {
            "text": "{用户输入的健身相关问题，附带上用户身体数据上下文}"
          }
        }
      ]
    }
  },
  "type": "query",
  "session_id": "{会话ID}",
  "project_id": "{项目ID}"
}
```

#### 上下文注入策略

为了使 Coze 智能体返回个性化的结果，后端在构造请求时需将用户上下文信息拼接到 `prompt.text` 中：

```
【用户档案】身高：175cm，体重：70kg，健身目标：增肌，运动等级：进阶
【近7日数据】打卡5次，总消耗2100kcal，主要运动：力量训练3次/跑步2次
【用户问题】帮我制定下周的健身计划
```

> 这样 Coze 智能体无需后端维护 Prompt 模板，用户画像数据随请求动态注入，智能体内部自动识别意图并路由。

#### 响应解析

Coze 返回 SSE 流，每行格式为 `data:{JSON}`，后端解析流程：

1. 遍历 `response.iter_lines()`
2. 过滤以 `data:` 开头的行
3. 去除 `data:` 前缀后解析 JSON
4. 提取智能体回复内容，转发给前端

### AI 调用逻辑补充

- **超时与降级**：Coze API 调用设置 30 秒超时，超时或异常时返回预设兜底文案（如"暂无法生成计划，请稍后再试或手动创建计划"）
- **调用频率控制**：每人每日 AI 调用上限 10 次，通过 Redis 计数器实现，每日零点重置
- **会话管理**：每个用户维护独立的 `session_id`，确保多轮对话上下文隔离，session_id 存储在 Redis 中并设置 24 小时过期
- **响应缓存**：相同参数的计划生成请求在 10 分钟内返回缓存结果，减少重复调用
- **流式转发**：后端将 Coze SSE 流实时转发至前端（使用 Spring 的 `SseEmitter`），前端逐字渲染实现打字机效果

### 技术约束

- 后端 SpringBoot 统一调用 Coze 工作流 API（通过 `gym_coze.py` 封装的客户端），前端仅做输入展示与流式渲染
- 实现**超时重试、错误降级**，API 异常返回兜底文案
- 每人每日 AI 调用限制 10 次，控制成本
- Coze 智能体的 Prompt 编排、意图路由、输出格式控制在 **Coze 平台**完成，后端不维护 Prompt 模板
- 敏感凭证（Coze API Token、项目 ID）通过 `.env` 注入，禁止硬编码

---

## 数据库设计

### 业务表清单

| 表名 | 说明 | 关键字段 |
|:---:|:---:|:---|
| `user` | 用户表 | id, username, password, role, avatar, height, weight, fitness_goal, fitness_level, status |
| `check_in` | 打卡记录表 | id, user_id, exercise_type, duration_minutes, calories, image_url, note, check_in_time |
| `plan` | 健身计划表 | id, user_id, plan_name, goal, difficulty, start_date, end_date, status, source(AI/手动/模板) |
| `plan_detail` | 计划明细表 | id, plan_id, day_of_week, exercise_type, sets, reps, duration, note |
| `post` | 社区动态表 | id, user_id, content, image_urls, like_count, comment_count, status |
| `comment` | 评论表 | id, post_id, user_id, parent_id(二级评论), content |
| `achievement` | 成就表 | id, user_id, type(7天/30天/100天), achieved_at |
| `ai_call_log` | AI 调用日志表 | id, user_id, call_type, session_id, request_summary, response_summary, created_at |

### 设计要点

- 所有表含 `created_at`、`updated_at`、`is_deleted`（逻辑删除）公共字段
- `user` 表的 `role` 字段用 TINYINT 枚举（0=普通用户, 1=督导, 2=管理员）
- `check_in` 表按 `user_id + DATE(check_in_time)` 建联合索引，支撑每日打卡次数限制查询
- `post` 的 `image_urls` 存 JSON 数组，单条动态最多 9 张图
- `ai_call_log` 增加 `session_id` 字段，关联 Coze 会话上下文，用于调用次数统计与成本审计
- `ai_call_log` 移除 `tokens_used` 字段（Coze 平台内部管理 token 消耗，后端无法直接获取准确值）

---

## 非功能性需求

### 后端规范

- RESTful 接口，SpringDoc 自动生成 Swagger 文档
- 统一异常处理`@RestControllerAdvice`
- Jakarta Validation 参数校验
- JWT 无状态身份认证
- **统一响应体**：所有接口返回 `{ code, message, data }` 格式，code 遵循 HTTP 语义（200 成功 / 400 参数错误 / 401 未认证 / 403 无权限 / 500 服务器异常）
- **分页封装**：列表接口统一使用 MyBatis-Plus 的 `Page` 对象，返回 `{ records, total, current, size }`
- **日志规范**：使用 SLF4J + Logback，关键操作（登录、打卡、AI 调用）记录 INFO 日志，异常记录 ERROR 日志含堆栈
- **SSE 支持**：AI 相关接口返回 `SseEmitter`，实现 Coze 流式响应到前端的透传

### 前端规范

- Vue3 + Vite + Tailwind CSS
- 路由守卫（登录状态 / 角色校验）
- 加载状态、错误提示、响应式布局
- **Axios 请求封装**：统一添加 JWT Token、统一错误拦截（401 自动跳转登录页）、统一 Loading 管理
- **组件设计**：遵循容器/展示组件分离原则，业务逻辑在容器组件，展示组件通过 Props 接收数据
- **移动端适配**：Tailwind 响应式断点（sm/md/lg），核心页面优先适配手机竖屏
- **SSE 客户端**：AI 对话界面使用 `EventSource` 或 `fetch` + `ReadableStream` 接收流式响应，逐字渲染打字机效果

### 部署规范

- Docker + Docker Compose 四服务部署（backend / frontend / db / redis）
- 容器网络互通，数据库数据持久化（MySQL volume + Redis volume）
- 一键启动，无环境依赖
- **环境变量**：数据库密码、JWT 密钥、Coze API Token、Coze Project ID 等敏感信息通过 `.env` 文件注入，不硬编码

### 安全规范

- 密码使用 BCrypt 加密存储，禁止明文
- JWT Token 有效期 2 小时，Refresh Token 有效期 7 天
- 所有接口入参做 XSS 过滤
- 文件上传限制类型（jpg/png/gif）与大小（≤5MB）
- AI 调用接口需鉴权，防止未登录滥用
- Coze API Token 不得写入前端代码或 Git 仓库，仅通过后端环境变量读取

---

## 业务规则

1. 用户每日打卡上限 3 次，防止数据刷取
2. AI 功能每人每日调用≤10 次
3. 计划连续 3 天未执行，系统自动提醒
4. 违规动态管理员可删除（软删除）
5. 健身数据仅本人与督导可见
6. 用户注册后需完善身体数据才能使用 AI 功能（确保 AI 输出质量）
7. 社区动态评论不可编辑，仅可删除（简化逻辑）
8. 排行榜数据每日凌晨定时更新，非实时查询（降低数据库压力）
9. 每个用户的 Coze `session_id` 独立维护，24 小时内复用同一会话以保持上下文连贯
10. AI 请求的 prompt 中自动注入用户身体数据与近期运动摘要，Coze 智能体基于上下文返回个性化结果

---

## 项目目录结构

```
gym-checkin/
├── docker-compose.yml
├── .env.example
├── sql/
│   └── schema.sql
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/gym/
│       ├── GymApplication.java
│       ├── config/              # Redis、CORS、Swagger 等配置
│       ├── security/            # JWT 过滤器与认证逻辑
│       ├── common/              # 统一响应体、全局异常、常量枚举
│       ├── controller/          # 各模块 Controller
│       ├── service/             # 业务逻辑层
│       ├── mapper/              # MyBatis-Plus Mapper
│       ├── entity/              # 数据库实体
│       ├── dto/                 # 请求/响应 DTO
│       └── coze/                # Coze Agent 集成封装
│           ├── CozeClient.java       # Coze API HTTP 客户端（SSE 流式）
│           ├── CozeConfig.java       # Coze 连接配置（Token / URL / Project ID）
│           ├── CozeSseEmitter.java   # SSE 流式转发封装
│           └── ContextBuilder.java   # 用户上下文组装（身体数据+运动摘要→prompt）
├── frontend/
│   ├── Dockerfile
│   ├── package.json
│   └── src/
│       ├── api/                 # Axios 请求封装与各模块 API
│       ├── assets/              # 静态资源
│       ├── components/          # 通用组件
│       ├── composables/         # 组合式函数
│       ├── layouts/             # 布局组件
│       ├── pages/               # 页面级组件
│       ├── router/              # 路由配置与守卫
│       ├── stores/              # Pinia 状态管理
│       └── utils/               # 工具函数
├── gym_coze.py                  # Coze API 调用参考实现（Python）
└── docs/
    ├── requirements.md
    ├── database_design.md
    ├── ai_module.md
    └── claude_sessions/
```

---

## 接口规划（核心 API 清单）

### 用户模块

| 方法 | 路径 | 说明 |
|:---:|:---:|:---|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/refresh` | 刷新 Token |
| GET | `/api/user/profile` | 获取个人信息 |
| PUT | `/api/user/profile` | 更新个人信息 |
| PUT | `/api/user/password` | 修改密码 |
| POST | `/api/user/avatar` | 上传头像 |

### 打卡模块

| 方法 | 路径 | 说明 |
|:---:|:---:|:---|
| POST | `/api/checkin` | 创建打卡记录 |
| GET | `/api/checkin/list` | 查询打卡记录（分页） |
| GET | `/api/checkin/calendar` | 打卡日历视图数据 |
| GET | `/api/checkin/today-count` | 今日打卡次数 |
| GET | `/api/checkin/export` | 导出打卡记录 |

### 计划模块

| 方法 | 路径 | 说明 |
|:---:|:---:|:---|
| POST | `/api/plan` | 创建健身计划 |
| GET | `/api/plan/list` | 我的计划列表 |
| GET | `/api/plan/{id}` | 计划详情（含明细） |
| PUT | `/api/plan/{id}` | 编辑计划 |
| PUT | `/api/plan/{id}/status` | 启用/停用计划 |
| GET | `/api/plan/templates` | 公共计划模板列表 |

### 数据统计模块

| 方法 | 路径 | 说明 |
|:---:|:---:|:---|
| GET | `/api/stats/summary` | 运动数据总览 |
| GET | `/api/stats/trend` | 周/月趋势数据 |
| GET | `/api/stats/ranking` | 排行榜数据 |

### 社区模块

| 方法 | 路径 | 说明 |
|:---:|:---:|:---|
| POST | `/api/post` | 发布动态 |
| GET | `/api/post/list` | 动态列表（分页） |
| DELETE | `/api/post/{id}` | 删除动态 |
| POST | `/api/post/{id}/like` | 点赞/取消点赞 |
| POST | `/api/comment` | 发表评论 |
| GET | `/api/comment/list` | 评论列表 |

### AI 模块（Coze Agent）

| 方法 | 路径 | 说明 | 响应方式 |
|:---:|:---:|:---:|:---:|
| POST | `/api/ai/chat` | AI 统一对话入口（流式） | SSE |
| POST | `/api/ai/plan` | AI 生成健身计划（流式） | SSE |
| POST | `/api/ai/analyze` | AI 分析运动数据（流式） | SSE |
| POST | `/api/ai/recipe` | AI 推荐食谱（流式） | SSE |
| POST | `/api/ai/exercise-guide` | AI 动作指导（流式） | SSE |
| GET | `/api/ai/quota` | 查询当日 AI 剩余调用次数 | JSON |

> AI 接口统一走 Coze 智能体的 `stream_run` 流式通道，后端通过 `SseEmitter` 透传，前端逐字渲染。
> `/api/ai/chat` 为通用对话入口，由 Coze 智能体内置意图识别自动路由到对应工作流节点；
> 其余 4 个专用接口在后端预构造特定上下文（如数据分析接口自动注入用户运动数据），引导智能体直接进入对应功能模块。

### 督导/管理员模块

| 方法 | 路径 | 说明 |
|:---:|:---:|:---|
| GET | `/api/admin/users` | 用户列表 |
| PUT | `/api/admin/user/{id}/status` | 启用/禁用用户 |
| DELETE | `/api/admin/post/{id}` | 删除违规动态 |
| POST | `/api/admin/remind` | 发送健身提醒 |
| GET | `/api/admin/dashboard` | 管理数据看板 |

---

## 前端页面规划

| 页面 | 路由 | 说明 |
|:---:|:---:|:---|
| 登录页 | `/login` | 登录/注册 Tab 切换 |
| 首页 | `/` | 今日打卡入口 + 数据概览 + AI 快捷入口 |
| 打卡页 | `/checkin` | 运动类型选择 + 时长输入 + 图片上传 |
| 打卡日历 | `/checkin/calendar` | 月历热力图展示打卡记录 |
| 我的计划 | `/plan` | 当前计划列表 + 计划详情 |
| 计划创建 | `/plan/create` | 手动创建 / AI 生成 切换 |
| 数据统计 | `/stats` | 图表化的运动数据分析 |
| 排行榜 | `/ranking` | 周/月打卡与消耗排行 |
| 社区 | `/community` | 动态 Feed 流 + 发帖入口 |
| AI 助手 | `/ai` | 五类 AI 功能统一入口（对话式交互 + 快捷功能卡） |
| 个人中心 | `/profile` | 个人信息编辑 + 成就展示 |
| 管理后台 | `/admin` | 督导/管理员专用（用户管理 + 数据看板 + 内容审核） |

### AI 助手页面详细设计

AI 助手页面采用**对话式交互 + 功能快捷入口**的混合模式：

- **顶部功能卡片区**：5 个快捷功能卡片（生成计划 / 数据分析 / 食谱推荐 / 动作指导 / 伤病恢复），点击后自动填充对应 Prompt 发送给 AI
- **下方对话区**：类 ChatGPT 的对话界面，支持流式打字机输出，用户也可自由输入
- **上下文提示**：对话区顶部常驻显示当前用户身体数据摘要，让用户明确 AI 已获知其信息

---

## 可行性分析

### 技术可行性

| 维度 | 评估 | 说明 |
|:---:|:---:|:---|
| SpringBoot3 + MyBatis-Plus | 高 | 成熟技术栈，社区资源丰富，CRUD 开发效率高 |
| Vue3 + Tailwind CSS | 高 | 组件化开发 + 原子化CSS，快速搭建响应式界面 |
| Coze Agent 智能体编排 | 高 | 智能体已编排完成，工作流路由稳定，API 接口明确，只需后端封装调用 |
| SSE 流式传输 | 高 | SpringBoot SseEmitter + 前端 EventSource，成熟方案 |
| Docker Compose 部署 | 高 | 标准化容器编排，一键启动无障碍 |
| Redis 缓存与计数 | 高 | 成熟方案，AI 调用计数与排行榜缓存天然适合 Redis |

### 风险与应对

| 风险 | 影响 | 应对策略 |
|:---:|:---:|:---|
| Coze 平台服务不可用 | 高 | 30s 超时降级 + 预设兜底文案；可预留直连大模型 API 作为备用通道 |
| Coze API Token 过期 | 中 | Token 有效期长（OAuth），定期检查；后端启动时校验连通性 |
| AI 生成内容质量不稳定 | 中 | Coze 工作流节点结构化输出 + 后端关键字段校验兜底 |
| SSE 连接中断 | 低 | 前端实现自动重连 + 断线恢复提示 |
| 图片上传占用服务器存储 | 低 | 限制单张 5MB + 个人累计上限 100MB + 可后续迁移至对象存储 |
| 并发打卡写入压力 | 低 | 校园场景并发量有限，MySQL 常规配置即可支撑 |

---

## 开发里程碑建议

| 阶段 | 周期 | 交付物 |
|:---:|:---:|:---|
| 第 1 阶段：基础框架搭建 | 3-4 天 | 项目骨架 + 数据库建表 + JWT 认证 + Docker 配置 |
| 第 2 阶段：核心业务开发 | 5-7 天 | 打卡模块 + 计划模块 + 用户中心 |
| 第 3 阶段：数据统计与社区 | 3-4 天 | 统计图表 + 社区动态 + 排行榜 |
| 第 4 阶段：AI 模块集成 | 2-3 天 | Coze API 封装 + SSE 流式转发 + 上下文注入 + 调用限流 + 降级策略 |
| 第 5 阶段：管理后台与打磨 | 2-3 天 | 督导/管理功能 + 交互优化 + Bug 修复 |
| 第 6 阶段：部署与文档 | 1-2 天 | Docker Compose 验证 + 文档完善 |

> AI 模块开发周期从原 3-4 天缩减至 2-3 天，因 Coze 智能体编排已完成核心工作，后端仅需封装 API 调用与流式转发。

---

## 技术选型

| 分类 | 技术 | 版本 / 说明 |
|:---:|:---:|:---:|
| 后端 | Java 17 + SpringBoot3 + Maven + MyBatis-Plus | SpringBoot 3.2.x、MP 3.5.3 |
| 缓存 | Redis | 7.x，用于 AI 调用计数 + 排行榜缓存 + Token 黑名单 + session_id 管理 |
| 数据库 | MySQL | 8.0 |
| 前端 | Vue3 + Vite + Pinia + Vue Router + Tailwind CSS | Vue3.4.x |
| 图表 | ECharts | 5.x，数据统计可视化 |
| API 文档 | SpringDoc OpenAPI | Swagger 自动生成 |
| 认证 | JWT | 前后端分离无状态认证 |
| 容器化 | Docker + Docker Compose | 四服务一键部署 |
| AI 工具 | Claude Code | 代码生成 / 调试 / 重构 |
| AI 引擎 | Coze Agent 智能体编排 | 工作流 API（stream_run）+ 五功能路由 |
| 流式通信 | SSE（Server-Sent Events） | 后端 SseEmitter + 前端 EventSource |

## Coze Agent 集成要点速查

### 后端集成清单

1. **CozeClient**：封装 HTTP POST 到 `stream_run`，处理 SSE 响应解析
2. **CozeConfig**：从 `.env` 读取 `COZE_API_URL`、`COZE_API_TOKEN`、`COZE_PROJECT_ID`
3. **CozeSseEmitter**：将 Coze SSE 流逐行转发为 Spring SseEmitter 事件
4. **ContextBuilder**：查询用户身体数据 + 近 7 日运动摘要，拼接为上下文前缀注入 prompt
5. **SessionManager**：Redis 存储用户 ↔ session_id 映射，24h TTL，支持多轮对话上下文

### 前端集成清单

1. **SSE 客户端**：`EventSource` 或 `fetch` + `ReadableStream` 接收流式响应
2. **打字机渲染**：逐字追加到对话气泡，配合光标闪烁动画
3. **快捷功能卡**：5 个预设 Prompt 模板，点击即发送
4. **额度展示**：调用 `/api/ai/quota` 显示当日剩余次数，次数为 0 时禁用入口

### Coze 平台配置参考

| 配置项 | 值 |
|:---:|:---|
| API Endpoint | `https://q9khtybwsw.coze.site/stream_run` |
| Project ID | `7640283069890609190` |
| 认证方式 | Bearer Token（OAuth Access Token） |
| 响应格式 | SSE（`data:{JSON}` 逐行） |
| 智能体功能模块 | 计划生成 / 数据分析 / 食谱推荐 / 动作指导 / 伤病恢复 |

## 提交物目录

- 需求文档：`/docs/requirements.md`
- 后端代码：`/backend`
- 前端代码：`/frontend`
- 数据库脚本：`/sql/schema.sql`
- 容器配置：`docker-compose.yml`、前后端`Dockerfile`
- AI 模块文档：`/docs/ai_module.md`
- 数据库设计：`/docs/database_design.md`
- Coze 调用参考：`/gym_coze.py`
- Claude 对话记录：`/docs/claude_sessions/`
