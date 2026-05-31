-- 腾跃校园健身打卡系统 - 数据库初始化脚本
-- MySQL 8.0

SET NAMES utf8mb4;

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


-- 测试数据脚本

USE gym_checkin;

-- ============================================================
-- 测试数据 - 覆盖全部功能模块
-- 所有用户密码均为 BCrypt 编码的 "123456"
-- BCrypt hash: $2a$10$sUt6jzVW6eQWBiX64VyvEeAvlKlA6tsJc2/DPZmQfS3qvGymDYv5a
-- ============================================================

-- ============================================================
-- 模块 1：用户数据（9人，涵盖全部角色和健身目标）
-- ============================================================
INSERT INTO `user` (username, password, role, height, weight, fitness_goal, fitness_level, status, coach_id) VALUES
('admin',     '$2a$10$sUt6jzVW6eQWBiX64VyvEeAvlKlA6tsJc2/DPZmQfS3qvGymDYv5a', 2, 178.0, 75.0, '增肌', '高级', 1, NULL),
('coach01',   '$2a$10$sUt6jzVW6eQWBiX64VyvEeAvlKlA6tsJc2/DPZmQfS3qvGymDYv5a', 1, 172.0, 68.0, '塑形', '高级', 1, NULL),
('zhangsan',  '$2a$10$sUt6jzVW6eQWBiX64VyvEeAvlKlA6tsJc2/DPZmQfS3qvGymDYv5a', 0, 175.0, 70.0, '增肌', '进阶', 1, 2),
('lisi',      '$2a$10$sUt6jzVW6eQWBiX64VyvEeAvlKlA6tsJc2/DPZmQfS3qvGymDYv5a', 0, 165.0, 55.0, '减脂', '入门', 1, 2),
('wangwu',    '$2a$10$sUt6jzVW6eQWBiX64VyvEeAvlKlA6tsJc2/DPZmQfS3qvGymDYv5a', 0, 180.0, 80.0, '增肌', '进阶', 1, 2),
('zhaoliu',   '$2a$10$sUt6jzVW6eQWBiX64VyvEeAvlKlA6tsJc2/DPZmQfS3qvGymDYv5a', 0, 160.0, 50.0, '塑形', '入门', 1, NULL),
('sunqi',     '$2a$10$sUt6jzVW6eQWBiX64VyvEeAvlKlA6tsJc2/DPZmQfS3qvGymDYv5a', 0, 170.0, 72.0, '减脂', '入门', 1, NULL),
('zhouba',    '$2a$10$sUt6jzVW6eQWBiX64VyvEeAvlKlA6tsJc2/DPZmQfS3qvGymDYv5a', 0, 182.0, 85.0, '增肌', '进阶', 1, NULL),
('coach02',   '$2a$10$sUt6jzVW6eQWBiX64VyvEeAvlKlA6tsJc2/DPZmQfS3qvGymDYv5a', 1, 168.0, 60.0, '保持健康', '高级', 1, NULL);

-- ============================================================
-- 模块 2：打卡记录（35条，覆盖14天+8种运动类型）
-- ============================================================
INSERT INTO `check_in` (user_id, exercise_type, duration_minutes, calories, note, check_in_time) VALUES
-- zhangsan(3) - 增肌进阶，连续7天打卡
(3, '力量训练', 60, 450, '胸肌+三头超级组',       DATE_SUB(NOW(), INTERVAL 14 DAY)),
(3, '跑步',      30, 280, '慢跑5公里放松',          DATE_SUB(NOW(), INTERVAL 13 DAY)),
(3, '力量训练', 45, 380, '背部训练日',              DATE_SUB(NOW(), INTERVAL 12 DAY)),
(3, 'HIIT',      25, 320, '高强度间歇20分钟极限',   DATE_SUB(NOW(), INTERVAL 11 DAY)),
(3, '游泳',      40, 350, '自由泳1000米',            DATE_SUB(NOW(), INTERVAL 10 DAY)),
(3, '力量训练', 60, 420, '腿部深蹲日，加练小腿',    DATE_SUB(NOW(), INTERVAL 9 DAY)),
(3, '瑜伽',      30, 150, '拉伸放松，缓解肌肉酸痛', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(3, '力量训练', 50, 400, '肩部推举+侧平举',        DATE_SUB(NOW(), INTERVAL 6 DAY)),
(3, '跑步',      35, 310, '间歇跑5组',              DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, '力量训练', 55, 430, '硬拉+划船，突破新重量',  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, '骑行',      45, 350, '户外骑行15公里',          DATE_SUB(NOW(), INTERVAL 1 DAY)),
-- lisi(4) - 减脂入门，以有氧为主
(4, '跑步',      40, 350, '晨跑5公里，配速6分半',   DATE_SUB(NOW(), INTERVAL 13 DAY)),
(4, '跳绳',      20, 200, '每分钟120次，暴汗',      DATE_SUB(NOW(), INTERVAL 11 DAY)),
(4, '瑜伽',      45, 180, '哈他瑜伽，身心放松',     DATE_SUB(NOW(), INTERVAL 9 DAY)),
(4, '跑步',      35, 300, '夜跑，状态不错',          DATE_SUB(NOW(), INTERVAL 7 DAY)),
(4, '骑行',      50, 380, '动感单车课程',            DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, '跑步',      30, 260, '法特莱克跑',              DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, '跳绳',      25, 250, '双摇练习',                DATE_SUB(NOW(), INTERVAL 1 DAY)),
-- wangwu(5) - 增肌进阶，大重量训练
(5, '力量训练', 70, 500, '全身复合动作日',          DATE_SUB(NOW(), INTERVAL 12 DAY)),
(5, 'HIIT',      30, 380, '战绳+波比跳循环',        DATE_SUB(NOW(), INTERVAL 10 DAY)),
(5, '力量训练', 55, 400, '肩部三角肌专项',          DATE_SUB(NOW(), INTERVAL 8 DAY)),
(5, '跑步',      25, 220, '冲刺跑5组',               DATE_SUB(NOW(), INTERVAL 6 DAY)),
(5, '力量训练', 65, 480, '胸部卧推+上斜',           DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, '拳击',      45, 420, '沙袋训练+空击',          DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, '力量训练', 50, 390, '手臂超级组',              NOW()),
-- zhaoliu(6) - 塑形入门
(6, '瑜伽',      50, 200, '流瑜伽初级',              DATE_SUB(NOW(), INTERVAL 10 DAY)),
(6, '普拉提',    40, 180, '核心训练',                DATE_SUB(NOW(), INTERVAL 7 DAY)),
(6, '跑步',      30, 250, '轻松跑',                  DATE_SUB(NOW(), INTERVAL 4 DAY)),
(6, '瑜伽',      60, 220, '阴瑜伽深度拉伸',          DATE_SUB(NOW(), INTERVAL 2 DAY)),
-- sunqi(7) - 新手，刚开始健身
(7, '跑步',      20, 160, '第一次打卡！操场5圈',     DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7, '跳绳',      15, 130, '基础跳绳，还在学习节奏', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(7, '跑步',      25, 190, '慢跑，比上次快了',        DATE_SUB(NOW(), INTERVAL 1 DAY)),
-- zhouba(8) - 增肌进阶，热衷登山
(8, '登山',     120, 600, '周末爬西山，海拔800m',   DATE_SUB(NOW(), INTERVAL 6 DAY)),
(8, '力量训练', 45, 380, '背部+二头',               DATE_SUB(NOW(), INTERVAL 3 DAY)),
(8, '跑步',      30, 270, '跑步机爬坡模式',         DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ============================================================
-- 模块 3：健身计划（6个，含个人计划+公共模板+AI生成）
-- ============================================================
INSERT INTO `plan` (user_id, plan_name, goal, difficulty, start_date, end_date, status, source, is_template) VALUES
-- 个人计划
(3, '增肌三分化训练',  '增肌', '进阶', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1, '手动', 0),
(4, '减脂入门计划',    '减脂', '入门', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 21 DAY), 1, '模板', 0),
(5, '力量突破五周计划','增肌', '进阶', DATE_SUB(CURDATE(), INTERVAL 7 DAY), DATE_ADD(CURDATE(), INTERVAL 28 DAY), 1, 'AI',   0),
(6, '塑形瑜伽21天',    '塑形', '入门', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 21 DAY), 1, '手动', 0),
(7, '新手上路两周计划','减脂', '入门', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY), 1, '模板', 0),
-- 公共模板（user_id=1 为 admin 创建的公共模板）
(1, '全身燃脂HIIT模板','减脂', '入门', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1, '手动', 1),
(1, '五分化健美模板',  '增肌', '高级', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 60 DAY), 1, 'AI',   1);

-- ============================================================
-- 模块 3-b：计划明细（对应上面计划1-5的每日训练安排）
-- ============================================================
INSERT INTO `plan_detail` (plan_id, day_of_week, exercise_type, sets, reps, duration, note) VALUES
-- 计划1：增肌三分化训练 (zhangsan)
(1, 1, '卧推',       4, 12,  0, '胸肌主项，控制离心'),
(1, 1, '哑铃飞鸟',   3, 15,  0, '胸肌辅助，顶峰收缩'),
(1, 1, '绳索下压',   3, 12,  0, '三头肌孤立'),
(1, 2, '引体向上',   4, 10,  0, '背部主项，宽握'),
(1, 2, '划船',       3, 12,  0, '背部厚度'),
(1, 2, '哑铃弯举',   3, 12,  0, '二头肌顶峰收缩'),
(1, 3, '深蹲',       4, 10,  0, '腿部主项，全蹲'),
(1, 3, '硬拉',       3,  8,  0, '后侧链强力训练'),
(1, 3, '提踵',       3, 20,  0, '小腿耐力'),
-- 计划2：减脂入门计划 (lisi)
(2, 1, '跑步',       0,  0, 30, '慢跑，心率保持在130-140'),
(2, 2, '瑜伽',       0,  0, 45, '哈他瑜伽基础'),
(2, 3, '跑步',       0,  0, 35, '间歇跑，快慢交替'),
-- 计划3：力量突破五周计划 (wangwu)
(3, 1, '深蹲',       5,  5,  0, '大重量低次数，冲击PR'),
(3, 1, '腿举',       3, 10,  0, '辅助腿部训练'),
(3, 2, '卧推',       5,  5,  0, '力量突破日'),
(3, 2, '双杠臂屈伸', 3, 12,  0, '三头+下胸'),
(3, 3, '硬拉',       5,  5,  0, '传统硬拉，核心收紧'),
(3, 3, '引体向上',   3,  8,  0, '负重引体'),
(3, 4, '推举',       4,  8,  0, '站姿杠铃推举'),
(3, 4, '侧平举',     4, 15,  0, '三角肌中束'),
(3, 5, '有氧日',     0,  0, 40, '游泳或骑行，主动恢复'),
-- 计划4：塑形瑜伽21天 (zhaoliu)
(4, 1, '瑜伽',       0,  0, 50, '流瑜伽 - 全身流动'),
(4, 2, '普拉提',     0,  0, 40, '核心力量训练'),
(4, 3, '瑜伽',       0,  0, 45, '哈他瑜伽 - 体式精讲'),
(4, 4, '普拉提',     0,  0, 40, '臀腿线条'),
(4, 5, '瑜伽',       0,  0, 60, '阴瑜伽 - 深度拉伸放松'),
-- 计划5：新手上路两周计划 (sunqi)
(5, 1, '跑步',       0,  0, 20, '慢跑+快走交替，适应运动节奏'),
(5, 2, '全身激活',   2, 12,  0, '徒手深蹲+俯卧撑+平板支撑'),
(5, 3, '跑步',       0,  0, 25, '慢跑，尝试连续跑不停'),
(5, 4, '全身激活',   2, 12,  0, '与第二天相同，逐渐适应'),
(5, 5, '跳绳',       0,  0, 15, '基础跳绳，学习手脚协调');

-- ============================================================
-- 模块 4：社区动态（8条，含不同用户+不同话题）
-- ============================================================
INSERT INTO `post` (user_id, content, like_count, comment_count, status) VALUES
(3, '今天力量训练状态不错，卧推突破80kg了！感觉自己离目标又近了一步💪', 12, 4, 1),
(4, '坚持跑步一周了，从跑3公里喘到不行到现在能跑5公里，体能明显提升！', 8, 2, 1),
(5, 'HIIT真的太累了，每次做完都想吐，但是效果真的很明显，两周体脂降了2%', 15, 5, 1),
(3, '分享一个增肌干货：三分化训练的核心是「渐进超负荷」，每周增加2.5kg，持续记录，进步看得见！', 20, 6, 1),
(4, '今天第一次尝试跳绳，跳了100个就喘得不行…有没有跳绳技巧分享？', 5, 3, 1),
(7, '新人报到！刚办了健身卡，主要是想减脂，求大佬们带我入门🙏', 10, 4, 1),
(8, '周末爬西山打卡！户外运动真的比健身房有趣多了，推荐大家都去试试登山', 18, 3, 1),
(6, '练了两个月的瑜伽，最大的感受是体态改善了很多，驼背问题基本解决了', 9, 2, 1);

-- ============================================================
-- 模块 4-b：评论（18条，含一级+二级评论+@回复）
-- ============================================================
INSERT INTO `comment` (post_id, user_id, parent_id, content) VALUES
-- post1 的评论
(1, 4, NULL, '太强了！80kg是我的两倍，膜拜大佬'),
(1, 5, NULL, '大佬带带我，我也在练卧推'),
(1, 3, 1, '哈哈坚持训练你也可以的！一起加油'),
(1, 8, NULL, '卧推80kg真的是一个里程碑，恭喜！'),
-- post2 的评论
(2, 3, NULL, '跑步是最棒的有氧，坚持住！'),
(2, 4, 5, '谢谢大佬鼓励，我会继续坚持的'),
-- post3 的评论
(3, 3, NULL, '两周降2%真的很猛，饮食有控制吗？'),
(3, 5, 7, '有的！碳水减了30%，蛋白质每天至少1.6g/kg体重'),
(3, 4, NULL, 'HIIT真的好难坚持，我试了一次就放弃了...'),
(3, 7, NULL, '我能问问什么是HIIT吗…我是新人不太懂'),
(3, 5, 10, 'HIIT就是高强度间歇训练，比如冲刺30秒休息15秒这样循环，新手建议从低强度开始哦'),
-- post4 的评论
(4, 5, NULL, '三分化确实是最适合新手的增肌方案，我也在用'),
(4, 6, NULL, '能介绍一下三分化具体怎么安排吗？'),
(4, 3, 13, '就是推/拉/腿三天循环，每次练不同的肌群，一周六练或者三天一循环'),
(4, 8, NULL, '想问一下饮食方面有什么建议？'),
(4, 3, 15, '增肌期热量盈余300-500大卡，蛋白质2g/kg体重，碳水4-5g/kg'),
-- post7 的评论
(7, 3, NULL, '登山确实很好！有氧+腿部+风景，一举三得'),
(7, 8, 17, '是的！下次准备挑战更高的山，大佬有兴趣一起吗？');

-- ============================================================
-- 模块 5：成就（覆盖多个用户的7天/30天成就）
-- ============================================================
INSERT INTO `achievement` (user_id, type, achieved_at) VALUES
(3, '7天',   DATE_SUB(NOW(), INTERVAL 30 DAY)),
(3, '30天',  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, '100天', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, '7天',   DATE_SUB(NOW(), INTERVAL 10 DAY)),
(4, '30天',  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, '7天',   DATE_SUB(NOW(), INTERVAL 20 DAY)),
(5, '30天',  DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, '7天',   DATE_SUB(NOW(), INTERVAL 15 DAY));

-- ============================================================
-- 模块 6：AI 调用日志（11条，覆盖全部6种调用类型）
-- ============================================================
INSERT INTO `ai_call_log` (user_id, call_type, session_id, request_summary, response_summary, created_at) VALUES
-- plan: 生成健身计划
(3, 'plan', 'sess_001', '帮我生成一份增肌三分化训练计划，为期四周', '已生成四周增肌三分化计划，包含推/拉/腿三个训练日', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(4, 'plan', 'sess_002', '我是新手女生，想减脂，帮我设计一个入门计划', '已生成减脂入门计划，以有氧为主配合基础力量训练', DATE_SUB(NOW(), INTERVAL 18 DAY)),
(5, 'plan', 'sess_003', '我想突破卧推100kg，帮我设计一个力量突破计划', '已生成五周力量突破计划，采用5x5训练法逐步加重', DATE_SUB(NOW(), INTERVAL 14 DAY)),
-- analyze: 数据分析
(3, 'analyze', 'sess_004', '分析我最近一个月的训练数据，看看有什么问题', '你的背部训练频率偏低，建议每周至少安排一次背部专项', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(5, 'analyze', 'sess_005', '帮我看看我的体脂变化趋势', '根据你的打卡数据，体脂率呈现下降趋势，继续保持', DATE_SUB(NOW(), INTERVAL 7 DAY)),
-- recipe: 食谱推荐
(4, 'recipe', 'sess_006', '推荐一份适合减脂的一日三餐食谱', '推荐了高蛋白低碳水的减脂食谱，总热量约1400大卡', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(5, 'recipe', 'sess_007', '增肌期需要补充什么营养？给个增肌食谱', '建议蛋白质2g/kg体重，配合复合碳水，推荐了增肌三餐', DATE_SUB(NOW(), INTERVAL 8 DAY)),
-- exercise_guide: 动作指导
(7, 'exercise_guide', 'sess_008', '深蹲的正确姿势是什么样的？怕受伤', '详细说明了深蹲的站距、下蹲深度、膝盖方向和常见错误', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(6, 'exercise_guide', 'sess_009', '瑜伽下犬式总是做不对，怎么纠正？', '讲解了手掌、肩膀、髋部三个关键点的正确位置', DATE_SUB(NOW(), INTERVAL 4 DAY)),
-- recovery: 伤病恢复
(8, 'recovery', 'sess_010', '跑步后膝盖有点疼，是跑步膝吗？怎么恢复？', '这可能是髌骨关节疼痛，建议休息3-5天，冰敷，加强股四头肌', DATE_SUB(NOW(), INTERVAL 3 DAY)),
-- chat: 自由对话
(4, 'chat', 'sess_011', '最近运动完总是很累，怎么判断是正常疲劳还是过度训练？', '如果持续疲劳超过3天、静息心率升高5次以上、睡眠质量下降，可能是过度训练', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ============================================================
-- 模块 7：提醒/异常记录（5条）
-- ============================================================
INSERT INTO `alert_log` (user_id, type, message, created_at) VALUES
-- remind: 计划连续3天未执行提醒
(3, 'remind', '你的「增肌三分化训练」计划已连续3天未执行打卡，建议尽快恢复训练哦！', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 'remind', '你的「减脂入门计划」已连续3天未执行，运动计划需要坚持才能看到效果！', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(6, 'remind', '你的「塑形瑜伽21天」计划连续3天未打卡，瑜伽需要持之以恒，加油！', NOW()),
-- anomaly: 异常打卡检测
(3, 'anomaly', '检测到今日打卡次数已达3次上限，请注意不要过度训练', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(7, 'anomaly', '你的运动时长较上周下降了60%，需要教练介入吗？', DATE_SUB(NOW(), INTERVAL 3 DAY));
