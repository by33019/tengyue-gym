# 腾跃 - 校园健身打卡智能计划助手

基于 SpringBoot3 + Vue3 + MySQL8 + Redis + Coze Agent 的校园健身管理平台。

## 快速启动

```bash
cp .env.example .env
# 编辑 .env 填入实际的 Coze API Token
docker compose up --build
```

启动后访问：
- 前端：http://localhost:3000
- 后端 API 文档：http://localhost:8080/swagger-ui.html

## 技术栈

- 后端：Java 17 + SpringBoot 3.2.x + MyBatis-Plus 3.5.3
- 前端：Vue 3.4.x + Vite + Pinia + Tailwind CSS
- 数据库：MySQL 8.0 + Redis 7.x
- AI：Coze Agent 智能体编排
- 部署：Docker + Docker Compose
