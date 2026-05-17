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
    is_anonymous TINYINT NOT NULL DEFAULT 0 COMMENT '0=实名, 1=匿名',
    coach_id BIGINT DEFAULT NULL COMMENT '所属教练ID',
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
    is_template TINYINT NOT NULL DEFAULT 0 COMMENT '0=个人计划, 1=公共模板',
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

-- 提醒/异常记录表
CREATE TABLE `alert_log` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT '类型: remind=提醒, anomaly=异常',
    message VARCHAR(500) DEFAULT NULL COMMENT '提醒内容',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_type_created (type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提醒/异常记录表';
