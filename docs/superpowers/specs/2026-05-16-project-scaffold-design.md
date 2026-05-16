# 项目脚手架搭建 - 设计文档

**日期**：2026-05-16

**状态**：已确认

**范围**：仅搭建项目骨架，不编写业务逻辑代码

## 技术栈（从 requirements.md 提取）

| 分类 | 技术 | 版本 |
|:---|:---|:---|
| 后端 | Java 17 + SpringBoot + Maven + MyBatis-Plus | SpringBoot 3.2.x, MP 3.5.3 |
| 前端 | Vue3 + Vite + Pinia + Vue Router + Tailwind CSS | Vue 3.4.x |
| 数据库 | MySQL + Redis | MySQL 8.0, Redis 7.x |
| 图表 | ECharts | 5.x |
| API 文档 | SpringDoc OpenAPI | - |
| 认证 | JWT | 无状态认证 |
| AI 引擎 | Coze Agent（SSE 流式） | stream_run API |
| 容器化 | Docker + Docker Compose | 四服务编排 |

## 目录结构

```
gym-checkin/
├── docker-compose.yml
├── .env.example
├── .gitignore
├── README.md
├── sql/
│   └── schema.sql
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/gym/
│       ├── GymApplication.java
│       ├── config/
│       ├── security/
│       ├── common/
│       │   ├── R.java
│       │   └── GlobalExceptionHandler.java
│       ├── controller/
│       ├── service/
│       ├── mapper/
│       ├── entity/
│       ├── dto/
│       └── coze/
│           ├── CozeClient.java
│           ├── CozeConfig.java
│           ├── CozeSseEmitter.java
│           └── ContextBuilder.java
│   └── src/main/resources/
│       └── application.yml
├── frontend/
│   ├── Dockerfile
│   ├── package.json
│   ├── vite.config.ts
│   ├── tailwind.config.js
│   ├── index.html
│   └── src/
│       ├── main.ts
│       ├── App.vue
│       ├── api/
│       ├── assets/
│       ├── components/
│       ├── composables/
│       ├── layouts/
│       ├── pages/
│       ├── router/
│       ├── stores/
│       └── utils/
└── docs/
```

## 脚手架包含内容

### 后端

- pom.xml：SpringBoot 3.2.x、MyBatis-Plus 3.5.3、Redis、JWT (jjwt)、SpringDoc、BCrypt、Lombok、MySQL 驱动
- application.yml：读环境变量，四段配置（datasource / redis / jwt / coze）
- GymApplication.java：SpringBoot 启动类
- common/R.java：统一响应体 `{ code, message, data }`
- common/GlobalExceptionHandler.java：`@RestControllerAdvice` 全局异常处理
- config/：CORS、Swagger (SpringDoc)、MyBatis-Plus 分页插件、Redis 序列化
- security/：JwtUtils（生成/校验/刷新）、JwtAuthFilter（OncePerRequestFilter）
- coze/：四个类的接口定义（CozeClient / CozeConfig / CozeSseEmitter / ContextBuilder），方法签名完整但不实现内部逻辑
- 预留包：controller / service / mapper / entity / dto

### 前端

- package.json：Vue3、Vite、Pinia、Vue Router、Tailwind CSS、Axios、ECharts
- vite.config.ts：代理配置（/api → backend:8080）
- tailwind.config.js：基础配置
- router/index.ts：12 条路由 + beforeEach 登录守卫
- api/request.ts：Axios 实例 + 拦截器（Token 注入/401 跳转）
- stores/user.ts：Pinia userStore（token/userInfo/login/logout）
- layouts/DefaultLayout.vue：基础壳（router-view + 导航占位）
- pages/：12 个占位组件

### 数据库

- schema.sql：9 张表完整 DDL（user / check_in / plan / plan_detail / post / comment / achievement / ai_call_log）含索引和公共字段

### 部署

- docker-compose.yml：backend / frontend / db / redis 四服务，网络 + 数据卷
- 后端 Dockerfile：Maven 编译 + JRE 运行（多阶段）
- 前端 Dockerfile：Node 编译 + Nginx 托管（多阶段）
- .env.example：DB_PASSWORD / JWT_SECRET / COZE_API_TOKEN / COZE_PROJECT_ID 等

## 不做的事情

- 不写业务 Controller / Service / Mapper
- 不写前端页面组件（仅空占位 `<template><div>页面名</div></template>`）
- 不实现 Coze 客户端具体逻辑
- 不写测试用例（业务开发阶段按 TDD 流程补充）
- 不修改已有的 doc/ 目录文件

## 行为准则（Karpathy 原则）

1. **先思后写**：每步操作前确认目标与边界
2. **简洁至上**：最少代码完成任务，不引入不必要的抽象
3. **精准修改**：只改脚手架相关，不顺手优化其他文件
4. **测试驱动**：脚手架阶段暂不写测试，业务开发阶段严格执行
