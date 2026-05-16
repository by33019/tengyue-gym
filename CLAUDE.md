# 腾跃 - 校园健身打卡智能计划助手

## 项目概述

基于 SpringBoot3 + Vue3 + MySQL8 + Redis + Docker + Coze Agent 的轻量化校园健身管理平台，实现打卡记录、计划定制、数据统计、AI 智能指导一体化服务。

## 技术栈

| 分类 | 技术 |
|:---|:---|
| 后端 | Java 17 + SpringBoot 3.2.x + Maven + MyBatis-Plus 3.5.3 |
| 前端 | Vue 3.4.x + Vite + Pinia + Vue Router + Tailwind CSS |
| 数据库 | MySQL 8.0 + Redis 7.x |
| 可视化 | ECharts 5.x |
| API 文档 | SpringDoc OpenAPI (Swagger) |
| 认证 | JWT 无状态认证 |
| AI 引擎 | Coze Agent 智能体编排 (stream_run API + SSE 流式) |
| 容器化 | Docker + Docker Compose (四服务一键部署) |

## 项目结构

```
gym-checkin/
├── docker-compose.yml
├── .env.example
├── sql/schema.sql
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/gym/
│       ├── GymApplication.java
│       ├── config/
│       ├── security/
│       ├── common/
│       ├── controller/
│       ├── service/
│       ├── mapper/
│       ├── entity/
│       ├── dto/
│       └── coze/
├── frontend/
│   ├── Dockerfile
│   ├── package.json
│   └── src/
├── gym_coze.py
└── docs/
```

## 用户角色

- **普通健身用户** (role=0)：注册登录、健身打卡、计划管理、数据查看、获取 AI 建议
- **健身督导** (role=1)：计划审核、打卡数据查看、健身提醒发布
- **系统管理员** (role=2)：用户管理、内容审核、系统配置、数据监控

## 核心业务模块

### 模块 1：用户认证与个人中心
- JWT 双 Token 机制（Access Token 2h + Refresh Token 7d）
- 个人信息管理（身高/体重/健身目标/运动基础）
- 身体数据档案与可视化、运动基础评估问卷、头像上传、账号软删除

### 模块 2：健身打卡与计划管理
- 运动打卡（类型/时长/消耗/配图），每日上限 3 次
- 健身计划 CRUD + 启用/停用 + 计划模板市场
- 打卡日历热力图、连续打卡成就徽章（7/30/100 天）
- 打卡图片自动水印、记录导出 Excel/CSV

### 模块 3：数据统计与健身社区
- 核心指标：运动总时长、总消耗、打卡天数、周环比/月同比
- ECharts 图表：周趋势折线图、运动类型饼图、月度热力图
- 排行榜（Top20，支持匿名）、二级评论、@提醒、举报

### AI 智能模块（Coze Agent 编排）
- 五大功能：计划生成 / 数据分析 / 食谱推荐 / 动作指导 / 伤病恢复
- Coze 工作流 API (`stream_run`) SSE 流式透传
- 每日调用上限 10 次（Redis 计数器），10 分钟重复请求缓存
- 30 秒超时降级，预设兜底文案
- 用户上下文自动注入（身体数据 + 近 7 日运动摘要）

## 后端规范

- RESTful 接口 + SpringDoc Swagger 文档
- 统一响应体：`{ code, message, data }`
- 分页封装：MyBatis-Plus Page，返回 `{ records, total, current, size }`
- 统一异常处理 `@RestControllerAdvice`
- Jakarta Validation 参数校验
- SLF4J + Logback 日志，关键操作 INFO，异常 ERROR
- 密码 BCrypt 加密，敏感凭证通过 `.env` 注入，禁止硬编码
- 文件上传限制 jpg/png/gif，≤5MB

## 前端规范

- Vue3 Composition API + Vite + Tailwind CSS
- 路由守卫（登录状态 + 角色校验）
- 容器/展示组件分离
- Axios 统一封装（JWT Token、401 拦截、Loading 管理）
- SSE 打字机效果（EventSource 或 fetch + ReadableStream）
- 响应式布局（sm/md/lg），核心页面优先适配手机竖屏

## 部署规范

- `docker compose up --build` 一键启动四服务（backend / frontend / db / redis）
- 数据持久化（MySQL volume + Redis volume）
- 环境变量通过 `.env` 文件注入

## 数据库表（8 张业务表 + AI 日志表）

| 表 | 说明 |
|:---|:---|
| user | 用户表（含身体数据与运动等级） |
| check_in | 打卡记录表 |
| plan | 健身计划表 |
| plan_detail | 计划明细表 |
| post | 社区动态表 |
| comment | 评论表（二级评论） |
| achievement | 成就表 |
| ai_call_log | AI 调用日志表 |

所有表含 `created_at`、`updated_at`、`is_deleted`（逻辑删除）。

## API 清单

- 用户模块：`/api/auth/*`、`/api/user/*`
- 打卡模块：`/api/checkin/*`
- 计划模块：`/api/plan/*`
- 统计模块：`/api/stats/*`
- 社区模块：`/api/post/*`、`/api/comment/*`
- AI 模块：`/api/ai/*`（SSE 流式响应）
- 管理模块：`/api/admin/*`

## 关键业务规则

1. 用户每日打卡上限 3 次
2. AI 功能每人每日调用 ≤10 次
3. 计划连续 3 天未执行，系统自动提醒
4. 违规动态管理员可软删除
5. 健身数据仅本人与督导可见
6. 用户注册后需完善身体数据才能使用 AI 功能
7. 排行榜每日凌晨定时更新，非实时查询
8. Coze session_id 独立维护，24h TTL

## Coze 集成要点

- API Endpoint：`https://q9khtybwsw.coze.site/stream_run`
- Project ID：`7640283069890609190`
- 认证：Bearer Token
- 响应：SSE (`data:{JSON}`)
- 后端组件：CozeClient、CozeConfig、CozeSseEmitter、ContextBuilder


## 请根据 doc/requirements.md 完成项目脚手架搭建：
1. 提取技术栈（若无则推荐并确认）
2. 初始化项目结构、安装依赖、创建目录、入口文件、脚本配置
3. 生成 .gitignore 和 README
4. 需要确认时暂停

## 行为准则（Karpathy 原则）
1. **先思后写**：澄清需求，列出假设。在产出代码前，先确认问题边界、技术方案，避免盲目编码。
2. **简洁至上**：避免过度抽象。用最少的代码实现功能，不提前引入设计模式或未来可能需要的灵活性。
3. **精准修改**：只改任务相关代码。不顺手“优化”无关模块，不进行与当前任务无关的重构。
4. **测试驱动**：先写失败测试，再实现功能。确保每个新功能都有对应的测试用例，且修改后所有测试通过。