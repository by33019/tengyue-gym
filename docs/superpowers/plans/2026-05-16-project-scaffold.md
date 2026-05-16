# 项目脚手架搭建 - 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 搭建腾跃校园健身打卡项目的完整脚手架，包括后端 SpringBoot3 项目、前端 Vue3 项目、数据库 DDL、Docker 编排，不含业务逻辑代码。

**Architecture:** 前后端分离，SpringBoot3 RESTful API + Vue3 SPA，MySQL8 持久化 + Redis7 缓存，Docker Compose 四服务一键部署。后端遵循 controller/service/mapper 分层，前端遵循 pages/components/api/stores 模块化。

**Tech Stack:** Java 17, SpringBoot 3.2.x, MyBatis-Plus 3.5.3, Vue 3.4.x, Vite, Pinia, Vue Router, Tailwind CSS, MySQL 8.0, Redis 7.x, Docker

---

### Task 1: 创建根目录基础文件

**Files:**
- Create: `.gitignore`
- Create: `.env.example`
- Create: `README.md`

- [ ] **Step 1: 创建 .gitignore**

写入内容：

```
# Java
target/
*.class
*.jar
*.war
!.mvn/wrapper/maven-wrapper.jar

# Node
node_modules/
dist/
.vite/

# IDE
.idea/
*.iml
.vscode/

# Docker
mysql_data/
redis_data/

# Env
.env
.env.local

# OS
.DS_Store
Thumbs.db

# Logs
*.log
logs/
```

- [ ] **Step 2: 创建 .env.example**

写入内容：

```env
# MySQL
MYSQL_ROOT_PASSWORD=root123
MYSQL_DATABASE=gym_checkin
MYSQL_USER=gym
MYSQL_PASSWORD=gym123

# Redis
REDIS_PASSWORD=

# JWT
JWT_SECRET=change-me-to-a-random-string
JWT_EXPIRATION=7200000
JWT_REFRESH_EXPIRATION=604800000

# Coze Agent
COZE_API_URL=https://q9khtybwsw.coze.site/stream_run
COZE_API_TOKEN=your-coze-token
COZE_PROJECT_ID=7640283069890609190
```

- [ ] **Step 3: 创建 README.md**

写入内容：

```markdown
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
```

- [ ] **Step 4: 提交**

```bash
git add .gitignore .env.example README.md
git commit -m "chore: add root project files (.gitignore, .env.example, README)"
```

---

### Task 2: 创建数据库初始化脚本

**Files:**
- Create: `sql/schema.sql`

- [ ] **Step 1: 创建 sql/schema.sql**

写入完整 DDL：

```sql
-- 腾跃校园健身打卡系统 - 数据库初始化脚本
-- MySQL 8.0

CREATE DATABASE IF NOT EXISTS gym_checkin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gym_checkin;

-- 用户表
CREATE TABLE `user` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role TINYINT NOT NULL DEFAULT 0 COMMENT '0=普通用户, 1=督导, 2=管理员',
    avatar VARCHAR(255) DEFAULT NULL,
    height DECIMAL(5,2) DEFAULT NULL COMMENT '身高cm',
    weight DECIMAL(5,2) DEFAULT NULL COMMENT '体重kg',
    fitness_goal VARCHAR(50) DEFAULT NULL COMMENT '健身目标: 减脂/增肌/塑形/保持健康',
    fitness_level VARCHAR(20) DEFAULT NULL COMMENT '运动等级: 入门/进阶/高级',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0=禁用, 1=正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 打卡记录表
CREATE TABLE `check_in` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exercise_type VARCHAR(50) NOT NULL COMMENT '运动类型',
    duration_minutes INT NOT NULL COMMENT '运动时长(分钟)',
    calories INT DEFAULT 0 COMMENT '消耗卡路里',
    image_url VARCHAR(255) DEFAULT NULL COMMENT '打卡图片',
    note VARCHAR(500) DEFAULT NULL COMMENT '备注',
    check_in_time DATETIME NOT NULL COMMENT '打卡时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user_date (user_id, check_in_time),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡记录表';

-- 健身计划表
CREATE TABLE `plan` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_name VARCHAR(100) NOT NULL,
    goal VARCHAR(50) NOT NULL,
    difficulty VARCHAR(20) NOT NULL DEFAULT '入门' COMMENT '难度: 入门/进阶/高级',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0=停用, 1=启用',
    source VARCHAR(20) NOT NULL DEFAULT '手动' COMMENT '来源: AI/手动/模板',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健身计划表';

-- 计划明细表
CREATE TABLE `plan_detail` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    day_of_week TINYINT NOT NULL COMMENT '星期几 1-7',
    exercise_type VARCHAR(50) NOT NULL,
    sets INT DEFAULT 3 COMMENT '组数',
    reps INT DEFAULT 12 COMMENT '每组次数',
    duration INT DEFAULT 30 COMMENT '时长(分钟)',
    note VARCHAR(255) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_plan_id (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='计划明细表';

-- 社区动态表
CREATE TABLE `post` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    image_urls JSON DEFAULT NULL COMMENT '图片URL数组，最多9张',
    like_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0=违规删除, 1=正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区动态表';

-- 评论表（二级评论）
CREATE TABLE `comment` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID，NULL=一级评论',
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_post_id (post_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 成就表
CREATE TABLE `achievement` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT '成就类型: 7天/30天/100天',
    achieved_at DATETIME NOT NULL COMMENT '获得时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_user_type (user_id, type),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成就表';

-- AI调用日志表
CREATE TABLE `ai_call_log` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    call_type VARCHAR(50) NOT NULL COMMENT '调用类型: plan/analyze/recipe/exercise_guide/recovery/chat',
    session_id VARCHAR(100) DEFAULT NULL COMMENT 'Coze会话ID',
    request_summary VARCHAR(500) DEFAULT NULL COMMENT '请求摘要',
    response_summary VARCHAR(1000) DEFAULT NULL COMMENT '响应摘要',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user_created (user_id, created_at),
    INDEX idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI调用日志表';
```

- [ ] **Step 2: 提交**

```bash
git add sql/schema.sql
git commit -m "feat: add database schema (9 tables)"
```

---

### Task 3: 创建 Docker Compose 编排

**Files:**
- Create: `docker-compose.yml`

- [ ] **Step 1: 创建 docker-compose.yml**

写入内容：

```yaml
version: '3.8'

services:
  db:
    image: mysql:8.0
    container_name: gym-db
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: gym-redis
    restart: unless-stopped
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: gym-backend
    restart: unless-stopped
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/${MYSQL_DATABASE}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
      SPRING_DATASOURCE_USERNAME: ${MYSQL_USER}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD}
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PASSWORD: ${REDIS_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION: ${JWT_EXPIRATION}
      JWT_REFRESH_EXPIRATION: ${JWT_REFRESH_EXPIRATION}
      COZE_API_URL: ${COZE_API_URL}
      COZE_API_TOKEN: ${COZE_API_TOKEN}
      COZE_PROJECT_ID: ${COZE_PROJECT_ID}
    ports:
      - "8080:8080"
    depends_on:
      db:
        condition: service_healthy
      redis:
        condition: service_healthy

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: gym-frontend
    restart: unless-stopped
    ports:
      - "3000:80"
    depends_on:
      - backend

volumes:
  mysql_data:
  redis_data:
```

- [ ] **Step 2: 提交**

```bash
git add docker-compose.yml
git commit -m "feat: add docker compose config (4 services)"
```

---

### Task 4: 创建后端 Maven 配置

**Files:**
- Create: `backend/pom.xml`

- [ ] **Step 1: 创建 backend/pom.xml**

写入内容：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>

    <groupId>com.gym</groupId>
    <artifactId>gym-checkin</artifactId>
    <version>1.0.0</version>
    <name>gym-checkin</name>

    <properties>
        <java.version>17</java.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <jjwt.version>0.12.5</jjwt.version>
    </properties>

    <dependencies>
        <!-- SpringBoot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- SpringBoot Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- MySQL Driver -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- BCrypt -->
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-crypto</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- SpringDoc OpenAPI -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.5.0</version>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建后端包目录结构**

```bash
mkdir -p backend/src/main/java/com/gym/{config,security,common,controller,service,mapper,entity,dto,coze}
mkdir -p backend/src/main/resources
mkdir -p backend/src/test/java/com/gym
```

- [ ] **Step 3: 提交**

```bash
git add backend/pom.xml backend/src/
git commit -m "feat: add backend Maven config and directory structure"
```

---

### Task 5: 创建后端核心配置与入口

**Files:**
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/java/com/gym/GymApplication.java`
- Create: `backend/src/main/java/com/gym/common/R.java`
- Create: `backend/src/main/java/com/gym/common/GlobalExceptionHandler.java`

- [ ] **Step 1: 创建 application.yml**

写入内容：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/gym_checkin?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai}
    username: ${SPRING_DATASOURCE_USERNAME:gym}
    password: ${SPRING_DATASOURCE_PASSWORD:gym123}
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: ${SPRING_REDIS_HOST:localhost}
      port: 6379
      password: ${SPRING_REDIS_PASSWORD:}

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0

jwt:
  secret: ${JWT_SECRET:default-secret-change-me}
  expiration: ${JWT_EXPIRATION:7200000}
  refresh-expiration: ${JWT_REFRESH_EXPIRATION:604800000}

coze:
  api-url: ${COZE_API_URL:https://q9khtybwsw.coze.site/stream_run}
  api-token: ${COZE_API_TOKEN:}
  project-id: ${COZE_PROJECT_ID:7640283069890609190}

springdoc:
  swagger-ui:
    path: /swagger-ui.html
  api-docs:
    path: /v3/api-docs
```

- [ ] **Step 2: 创建 GymApplication.java**

写入内容：

```java
package com.gym;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.gym.mapper")
public class GymApplication {
    public static void main(String[] args) {
        SpringApplication.run(GymApplication.class, args);
    }
}
```

- [ ] **Step 3: 创建 R.java**

写入内容：

```java
package com.gym.common;

import lombok.Data;

@Data
public class R<T> {
    private int code;
    private String message;
    private T data;

    private R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> R<T> ok(T data) {
        return new R<>(200, "success", data);
    }

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }

    public static <T> R<T> fail(String message) {
        return fail(400, message);
    }

    public static <T> R<T> unauthorized() {
        return fail(401, "未登录或登录已过期");
    }

    public static <T> R<T> forbidden() {
        return fail(403, "无权限");
    }

    public static <T> R<T> serverError() {
        return fail(500, "服务器异常");
    }
}
```

- [ ] **Step 4: 创建 GlobalExceptionHandler.java**

写入内容：

```java
package com.gym.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return R.fail(msg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleException(Exception e) {
        log.error("未捕获异常", e);
        return R.serverError();
    }
}
```

- [ ] **Step 5: 提交**

```bash
git add backend/src/main/resources/application.yml backend/src/main/java/com/gym/GymApplication.java backend/src/main/java/com/gym/common/
git commit -m "feat: add backend entry point, unified response, and exception handler"
```

---

### Task 6: 创建后端配置类

**Files:**
- Create: `backend/src/main/java/com/gym/config/CorsConfig.java`
- Create: `backend/src/main/java/com/gym/config/SwaggerConfig.java`
- Create: `backend/src/main/java/com/gym/config/MybatisPlusConfig.java`
- Create: `backend/src/main/java/com/gym/config/RedisConfig.java`
- Create: `backend/src/main/java/com/gym/config/PasswordConfig.java`

- [ ] **Step 1: 创建 CorsConfig.java**

```java
package com.gym.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

- [ ] **Step 2: 创建 SwaggerConfig.java**

```java
package com.gym.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("腾跃校园健身打卡 API")
                        .version("1.0.0")
                        .description("校园健身打卡智能计划助手接口文档"));
    }
}
```

- [ ] **Step 3: 创建 MybatisPlusConfig.java**

```java
package com.gym.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

- [ ] **Step 4: 创建 RedisConfig.java**

```java
package com.gym.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

- [ ] **Step 5: 创建 PasswordConfig.java**

```java
package com.gym.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 6: 提交**

```bash
git add backend/src/main/java/com/gym/config/
git commit -m "feat: add backend config classes (CORS, Swagger, MyBatis-Plus, Redis, BCrypt)"
```

---

### Task 7: 创建后端 JWT 认证

**Files:**
- Create: `backend/src/main/java/com/gym/security/JwtUtils.java`
- Create: `backend/src/main/java/com/gym/security/JwtAuthFilter.java`

- [ ] **Step 1: 创建 JwtUtils.java**

```java
package com.gym.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    private final SecretKey key;
    private final long expiration;
    private final long refreshExpiration;

    public JwtUtils(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateToken(Long userId, String username, int role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

- [ ] **Step 2: 创建 JwtAuthFilter.java**

```java
package com.gym.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.common.R;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // 放行路径
        if (path.startsWith("/api/auth/") || path.startsWith("/swagger") || path.startsWith("/v3/")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            writeUnauthorized(response);
            return;
        }

        Claims claims = jwtUtils.parseToken(token);
        request.setAttribute("userId", Long.valueOf(claims.getSubject()));
        request.setAttribute("username", claims.get("username", String.class));
        request.setAttribute("role", claims.get("role", Integer.class));

        chain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(R.unauthorized()));
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add backend/src/main/java/com/gym/security/
git commit -m "feat: add JWT authentication (utils + filter)"
```

---

### Task 8: 创建后端 Coze 集成骨架

**Files:**
- Create: `backend/src/main/java/com/gym/coze/CozeConfig.java`
- Create: `backend/src/main/java/com/gym/coze/CozeClient.java`
- Create: `backend/src/main/java/com/gym/coze/CozeSseEmitter.java`
- Create: `backend/src/main/java/com/gym/coze/ContextBuilder.java`

- [ ] **Step 1: 创建 CozeConfig.java**

```java
package com.gym.coze;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "coze")
public class CozeConfig {
    private String apiUrl;
    private String apiToken;
    private String projectId;
}
```

- [ ] **Step 2: 创建 CozeClient.java**

```java
package com.gym.coze;

/**
 * Coze API HTTP客户端，处理SSE流式请求
 */
public interface CozeClient {

    /**
     * 发送请求到Coze工作流并返回SSE流
     */
    void streamRun(String prompt, String sessionId, CozeSseEmitter emitter);
}
```

- [ ] **Step 3: 创建 CozeSseEmitter.java**

```java
package com.gym.coze;

/**
 * SSE流式转发封装
 */
public interface CozeSseEmitter {

    void send(String data);

    void complete();

    void error(Throwable t);

    boolean isCompleted();
}
```

- [ ] **Step 4: 创建 ContextBuilder.java**

```java
package com.gym.coze;

/**
 * 用户上下文组装，将身体数据与运动摘要拼接为prompt前缀
 */
public interface ContextBuilder {

    /**
     * 为指定用户构建AI请求的上下文文本
     */
    String buildContext(Long userId);
}
```

- [ ] **Step 5: 提交**

```bash
git add backend/src/main/java/com/gym/coze/
git commit -m "feat: add Coze integration interfaces"
```

---

### Task 9: 创建后端 Dockerfile

**Files:**
- Create: `backend/Dockerfile`

- [ ] **Step 1: 创建 backend/Dockerfile**

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 2: 提交**

```bash
git add backend/Dockerfile
git commit -m "feat: add backend Dockerfile (multi-stage build)"
```

---

### Task 10: 创建前端项目配置

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/vite.config.ts`
- Create: `frontend/tailwind.config.js`
- Create: `frontend/postcss.config.js`
- Create: `frontend/index.html`
- Create: `frontend/tsconfig.json`
- Create: `frontend/tsconfig.node.json`
- Create: `frontend/env.d.ts`

- [ ] **Step 1: 创建 package.json**

```json
{
  "name": "gym-checkin-frontend",
  "version": "1.0.0",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "axios": "^1.7.2",
    "echarts": "^5.5.0",
    "pinia": "^2.1.7",
    "vue": "^3.4.27",
    "vue-router": "^4.3.2"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.4",
    "autoprefixer": "^10.4.19",
    "postcss": "^8.4.38",
    "tailwindcss": "^3.4.3",
    "typescript": "^5.4.5",
    "vite": "^5.2.11",
    "vue-tsc": "^2.0.19"
  }
}
```

- [ ] **Step 2: 创建 vite.config.ts**

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

- [ ] **Step 3: 创建 tailwind.config.js**

```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}
```

- [ ] **Step 4: 创建 postcss.config.js**

```javascript
export default {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
}
```

- [ ] **Step 5: 创建 index.html**

```html
<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>腾跃 - 校园健身打卡</title>
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.ts"></script>
  </body>
</html>
```

- [ ] **Step 6: 创建 tsconfig.json**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "ESNext",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "preserve",
    "strict": true,
    "noUnusedLocals": false,
    "noUnusedParameters": false,
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["src/**/*.ts", "src/**/*.tsx", "src/**/*.vue", "env.d.ts"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

- [ ] **Step 7: 创建 tsconfig.node.json**

```json
{
  "compilerOptions": {
    "composite": true,
    "skipLibCheck": true,
    "module": "ESNext",
    "moduleResolution": "bundler",
    "allowSyntheticDefaultImports": true
  },
  "include": ["vite.config.ts"]
}
```

- [ ] **Step 8: 创建 env.d.ts**

```typescript
/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}
```

- [ ] **Step 9: 提交**

```bash
git add frontend/package.json frontend/vite.config.ts frontend/tailwind.config.js frontend/postcss.config.js frontend/index.html frontend/tsconfig.json frontend/tsconfig.node.json frontend/env.d.ts
git commit -m "feat: add frontend project configs (Vue3 + Vite + Tailwind + TS)"
```

---

### Task 11: 创建前端入口与核心模块

**Files:**
- Create: `frontend/src/main.ts`
- Create: `frontend/src/App.vue`
- Create: `frontend/src/style.css`
- Create: `frontend/src/api/request.ts`
- Create: `frontend/src/router/index.ts`
- Create: `frontend/src/stores/user.ts`

- [ ] **Step 1: 创建 main.ts**

```typescript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './style.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
```

- [ ] **Step 2: 创建 App.vue**

```vue
<template>
  <router-view />
</template>
```

- [ ] **Step 3: 创建 style.css**

```css
@tailwind base;
@tailwind components;
@tailwind utilities;
```

- [ ] **Step 4: 创建 api/request.ts**

```typescript
import axios from 'axios'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use(config => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

request.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default request
```

- [ ] **Step 5: 创建 router/index.ts**

```typescript
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'Login', component: () => import('@/pages/LoginPage.vue') },
    { path: '/', name: 'Home', component: () => import('@/pages/HomePage.vue') },
    { path: '/checkin', name: 'Checkin', component: () => import('@/pages/CheckinPage.vue') },
    { path: '/checkin/calendar', name: 'Calendar', component: () => import('@/pages/CalendarPage.vue') },
    { path: '/plan', name: 'Plan', component: () => import('@/pages/PlanPage.vue') },
    { path: '/plan/create', name: 'PlanCreate', component: () => import('@/pages/PlanCreatePage.vue') },
    { path: '/stats', name: 'Stats', component: () => import('@/pages/StatsPage.vue') },
    { path: '/ranking', name: 'Ranking', component: () => import('@/pages/RankingPage.vue') },
    { path: '/community', name: 'Community', component: () => import('@/pages/CommunityPage.vue') },
    { path: '/ai', name: 'Ai', component: () => import('@/pages/AiPage.vue') },
    { path: '/profile', name: 'Profile', component: () => import('@/pages/ProfilePage.vue') },
    { path: '/admin', name: 'Admin', component: () => import('@/pages/AdminPage.vue') }
  ]
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  if (to.path !== '/login' && !userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
```

- [ ] **Step 6: 创建 stores/user.ts**

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userInfo = ref<any>(null)

  function setToken(access: string, refresh: string) {
    token.value = access
    refreshToken.value = refresh
    localStorage.setItem('token', access)
    localStorage.setItem('refreshToken', refresh)
  }

  function setUserInfo(info: any) {
    userInfo.value = info
  }

  function logout() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }

  return { token, refreshToken, userInfo, setToken, setUserInfo, logout }
})
```

- [ ] **Step 7: 创建前端目录结构**

```bash
mkdir -p frontend/src/{api,assets,components,composables,layouts,pages,router,stores,utils}
mkdir -p frontend/public
```

- [ ] **Step 8: 提交**

```bash
git add frontend/src/
git commit -m "feat: add frontend entry, router, store, and axios setup"
```

---

### Task 12: 创建前端占位页面与布局

**Files:**
- Create: `frontend/src/layouts/DefaultLayout.vue`
- Create: `frontend/src/pages/LoginPage.vue`
- Create: `frontend/src/pages/HomePage.vue`
- Create: `frontend/src/pages/CheckinPage.vue`
- Create: `frontend/src/pages/CalendarPage.vue`
- Create: `frontend/src/pages/PlanPage.vue`
- Create: `frontend/src/pages/PlanCreatePage.vue`
- Create: `frontend/src/pages/StatsPage.vue`
- Create: `frontend/src/pages/RankingPage.vue`
- Create: `frontend/src/pages/CommunityPage.vue`
- Create: `frontend/src/pages/AiPage.vue`
- Create: `frontend/src/pages/ProfilePage.vue`
- Create: `frontend/src/pages/AdminPage.vue`

- [ ] **Step 1: 创建 DefaultLayout.vue**

```vue
<template>
  <div class="min-h-screen bg-gray-50">
    <router-view />
  </div>
</template>
```

- [ ] **Step 2: 批量创建占位页面**

LoginPage.vue：
```vue
<template>
  <div class="flex items-center justify-center min-h-screen">
    <div class="w-full max-w-md p-8 bg-white rounded-xl shadow-lg">
      <h1 class="text-2xl font-bold text-center mb-6">腾跃健身</h1>
      <div class="space-y-4">
        <input class="w-full px-4 py-2 border rounded-lg" placeholder="用户名" />
        <input class="w-full px-4 py-2 border rounded-lg" type="password" placeholder="密码" />
        <button class="w-full py-2 bg-blue-500 text-white rounded-lg">登录</button>
      </div>
    </div>
  </div>
</template>
```

HomePage.vue：
```vue
<template>
  <div class="p-4">
    <h1 class="text-xl font-bold">首页</h1>
  </div>
</template>
```

其余 10 个页面均为相同结构，替换页面名即可：
```vue
<template>
  <div class="p-4">
    <h1 class="text-xl font-bold">[页面名]</h1>
  </div>
</template>
```

- [ ] **Step 3: 提交**

```bash
git add frontend/src/pages/ frontend/src/layouts/
git commit -m "feat: add frontend placeholder pages (12 routes) and layout"
```

---

### Task 13: 创建前端 Dockerfile

**Files:**
- Create: `frontend/Dockerfile`

- [ ] **Step 1: 创建 frontend/Dockerfile**

```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json .
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

- [ ] **Step 2: 创建 frontend/nginx.conf**

```nginx
server {
    listen 80;
    server_name localhost;

    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add frontend/Dockerfile frontend/nginx.conf
git commit -m "feat: add frontend Dockerfile (multi-stage + nginx)"
```

---

### Task 14: 验证与收尾

- [ ] **Step 1: 验证项目目录结构完整**

```bash
find . -not -path './.git/*' -not -path './node_modules/*' -not -path './target/*' | sort
```

- [ ] **Step 2: 创建前端 public/vite.svg**

使用一个简单的 SVG 占位图标。

- [ ] **Step 3: 最终提交**

```bash
git add -A
git commit -m "chore: finalize project scaffold"
```
