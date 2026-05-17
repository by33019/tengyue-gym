# 腾跃 - 校园健身打卡智能计划助手

基于 SpringBoot3 + Vue3 + MySQL8 + Redis + Coze Agent 的轻量化校园健身管理平台。

## 快速启动

### Docker 一键部署
```bash
cp .env.example .env
# 编辑 .env 填入实际的 Coze API Token
docker compose up --build
```

### 本地开发
```bash
# 前置：启动 MySQL、Redis、MinIO

# 后端
cd backend
COZE_API_TOKEN="你的token" COZE_API_URL="..." COZE_PROJECT_ID="..." mvn spring-boot:run

# 前端
cd frontend
npm install
npm run dev
```

启动后访问：
- 前端：`http://localhost:3000`
- API 文档：`http://localhost:8080/swagger-ui.html`

## 测试账号

| 角色 | 用户名 | 密码 |
|:---|:---|:---|
| 管理员 | admin | 123456 |
| 教练 | coach01 / coach02 / coach03 | 123456 |
| 学员 | zhangsan / lisi / wangwu 等 | 123456 |

## 技术栈

| 分类 | 技术 |
|:---|:---|
| 后端 | Java 17 + SpringBoot 3.2 + Maven + MyBatis-Plus 3.5 |
| 前端 | Vue 3.4 + Vite + Pinia + Vue Router + Tailwind CSS |
| 数据库 | MySQL 8.0 + Redis 7.x |
| 存储 | MinIO（头像/图片上传） |
| AI | Coze Agent 工作流（SSE 流式响应） |
| 可视化 | ECharts 5.x |
| API 文档 | SpringDoc OpenAPI (Swagger) |
| 部署 | Docker + Docker Compose（五服务一键启动） |

## 项目结构

```
├── docker-compose.yml
├── .env / .env.example
├── sql/
│   ├── schema.sql              # 数据库建表脚本
│   └── test-data.sql           # 测试数据生成
├── doc/
│   ├── requirements.md         # 需求文档
│   ├── 开发日志.md             # 开发对话实录
│   └── gym_coze.py            # Coze API 参考实现
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/gym/
│       ├── GymApplication.java
│       ├── config/             # Redis / MinIO / Security / Async
│       ├── security/           # JWT 认证（双Token机制）
│       ├── common/             # 统一响应体 R<T> / 全局异常处理
│       ├── controller/         # 8 个控制器
│       ├── service/            # 业务层（含 AI 编排）
│       ├── mapper/             # MyBatis-Plus Mapper
│       ├── entity/             # 9 个实体类
│       ├── dto/                # 请求/响应 DTO
│       └── coze/               # Coze Agent 集成（HTTP 客户端 / SSE）
└── frontend/
    ├── Dockerfile
    ├── nginx.conf
    └── src/
        ├── api/                # Axios 封装 + 7 个 API 模块
        ├── components/         # Sidebar 侧边栏
        ├── layouts/            # DefaultLayout 响应式布局
        ├── pages/              # 15 个页面
        ├── router/             # 路由守卫（角色权限）
        └── stores/             # Pinia 用户状态管理
```

## 数据库表

| 表 | 说明 | 关键字段 |
|:---|:---|:---|
| `user` | 用户表 | role(0学员/1教练/2管理员), coach_id, is_anonymous |
| `check_in` | 打卡记录 | exercise_type, duration_minutes, calories |
| `plan` | 健身计划 | is_template, difficulty, source |
| `plan_detail` | 计划明细 | day_of_week, exercise_type, sets, reps |
| `post` | 社区动态 | content, like_count, comment_count |
| `comment` | 评论（二级） | parent_id |
| `achievement` | 成就徽章 | type(7天/30天/100天) |
| `ai_call_log` | AI 调用日志 | call_type, session_id |
| `alert_log` | 提醒/异常记录 | type(remind/anomaly) |

## 功能清单

### 用户认证与个人中心
- JWT 双 Token 机制（Access 2h + Refresh 7d）
- 注册可选角色（普通用户 / 教练）
- 个人信息管理（身高、体重、健身目标、运动等级）
- 头像上传（MinIO）+ 侧边栏实时同步
- 匿名模式开关（排行榜/社区可隐藏真实姓名）
- 密码修改 + 账号状态

### 健身打卡
- 运动打卡（类型、时长、消耗、配图、备注）
- 每日上限 3 次
- 打卡日历热力图（按月展示）
- 连续打卡天数统计
- 记录导出 Excel / CSV
- 打卡图片水印（后端已实现）

### 训练计划
- 计划 CRUD + 明细管理（按星期排课）
- 启用 / 停用状态切换
- 计划模板市场（教练可发布公共模板）
- 学员一键套用模板（自动按运动等级调整）
- 难度自适应（入门 / 进阶 / 高级）

### 数据统计与社区
- 核心指标：运动总时长、消耗、打卡天数、周环比
- ECharts 图表：周趋势折线图、运动类型饼图、月度热力图
- 排行榜 Top20（支持匿名模式）
- 社区动态（发布 / 点赞 / 二级评论 / @提醒）
- **敏感词过滤**：~250 词词库，发帖自动检测拦截

### AI 智能模块（Coze Agent）
- 五大功能：计划生成 / 数据分析 / 食谱推荐 / 动作指导 / 伤病恢复
- Coze 工作流 `stream_run` API + SSE 流式透传
- 前端打字机逐字渲染效果（15ms 延迟）
- 每日调用上限 10 次（管理员不限）
- 10 分钟内重复请求缓存
- 30 秒超时降级（6 种兜底文案）
- 用户上下文自动注入（身体数据 + 近 7 日运动摘要）

### 角色权限体系

| 角色 | 可见功能 |
|:---|:---|
| 学员 (0) | 首页、打卡、计划、统计、排行、社区、AI、个人中心 |
| 教练 (1) | 工作台、打卡、计划(发模板)、AI、学员管理、个人中心 |
| 管理员 (2) | 管理台、用户管理、内容审核、个人中心 |

- 前端路由守卫 + 后端 API 权限校验双重保障
- 教练可分配/移除学员、发送打卡提醒
- 管理员可启用/禁用/删除用户、审核删除动态
- 登录后按角色自动分流跳转

### 成就系统
- 连续打卡 7 / 30 / 100 天自动发放徽章
- 打卡后自动检测并即时发放
- 个人中心勋章墙展示

### 界面设计
- 桌面端：240px 固定侧边栏 + 全宽内容区
- 移动端：底部导航栏（自适应）
- 活力运动风配色（珊瑚红 + 荧光绿 + 深紫黑背景）
- 首页渐变横幅 + 运动风仪表盘

## API 清单

| 模块 | 端点 | 说明 |
|:---|:---|:---|
| 认证 | `POST /api/auth/register` | 注册（可选角色） |
| 认证 | `POST /api/auth/login` | 登录 |
| 认证 | `POST /api/auth/refresh` | 刷新 Token |
| 用户 | `GET/PUT /api/user/profile` | 个人信息 CRUD |
| 用户 | `POST /api/user/avatar` | 头像上传 |
| 用户 | `PUT /api/user/password` | 修改密码 |
| 用户 | `PUT /api/user/anonymous` | 匿名模式开关 |
| 用户 | `GET /api/user/achievements` | 成就列表 |
| 打卡 | `POST /api/checkin` | 创建打卡 |
| 打卡 | `GET /api/checkin/list` | 打卡列表 |
| 打卡 | `GET /api/checkin/calendar` | 日历热力图 |
| 打卡 | `GET /api/checkin/today-count` | 今日打卡次数 |
| 打卡 | `GET /api/checkin/export` | 导出 CSV/Excel |
| 计划 | `POST/GET/PUT /api/plan` | 计划 CRUD |
| 计划 | `PUT /api/plan/{id}/publish` | 发布/取消模板 |
| 统计 | `GET /api/stats/summary` | 数据总览 |
| 统计 | `GET /api/stats/trend` | 周趋势 |
| 排行 | `GET /api/stats/ranking` | 排行榜 |
| 社区 | `POST/GET /api/post` | 动态发布/列表 |
| 社区 | `POST /api/comment` | 评论 |
| AI | `POST /api/ai/{type}` | SSE 流式 AI（5 种类型） |
| AI | `GET /api/ai/quota` | 查询剩余额度 |
| 管理 | `GET /api/admin/dashboard` | 工作台概览 |
| 管理 | `GET/PUT/DELETE /api/admin/user` | 用户管理 |
| 管理 | `GET /api/admin/my-users` | 教练查看学员 |
| 管理 | `POST /api/admin/remind` | 批量提醒 |
| 管理 | `GET/PUT /api/admin/assign-student` | 教练分配学员 |
| 管理 | `GET/DELETE /api/admin/post` | 内容审核 |
| 管理 | `GET /api/admin/alerts` | 提醒/异常记录 |
| 管理 | `GET /api/admin/coaches` | 教练列表 |
