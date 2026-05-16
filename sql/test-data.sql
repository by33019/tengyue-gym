-- 测试数据脚本

USE gym_checkin;

-- 插入测试用户 (密码均为 BCrypt 编码的 "123456")
-- BCrypt hash for "123456": $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh
INSERT INTO `user` (username, password, role, height, weight, fitness_goal, fitness_level, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 2, 178.0, 75.0, '增肌', '高级', 1),
('coach01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 1, 172.0, 68.0, '塑形', '高级', 1),
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 0, 175.0, 70.0, '增肌', '进阶', 1),
('lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 0, 165.0, 55.0, '减脂', '入门', 1),
('wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 0, 180.0, 80.0, '增肌', '进阶', 1),
('zhaoliu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 0, 160.0, 50.0, '塑形', '入门', 1);

-- 插入打卡记录 (最近 7 天)
INSERT INTO `check_in` (user_id, exercise_type, duration_minutes, calories, note, check_in_time) VALUES
(3, '力量训练', 60, 450, '胸肌+三头', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(3, '跑步', 30, 280, '慢跑5公里', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, '力量训练', 45, 380, '背部训练', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 'HIIT', 25, 320, '高强度间歇', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, '游泳', 40, 350, '自由泳', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, '力量训练', 60, 420, '腿部训练', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, '瑜伽', 30, 150, '拉伸放松', NOW()),
(4, '跑步', 40, 350, '晨跑', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, '瑜伽', 45, 180, '哈他瑜伽', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, '跑步', 35, 300, '夜跑', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, '力量训练', 70, 500, '全身力量', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(5, 'HIIT', 30, 380, '', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, '力量训练', 55, 400, '肩部训练', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, '跑步', 25, 220, '', NOW()),
(6, '瑜伽', 50, 200, '流瑜伽', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, '跑步', 30, 250, '', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 插入健身计划
INSERT INTO `plan` (user_id, plan_name, goal, difficulty, start_date, end_date, status, source) VALUES
(3, '增肌三分化训练', '增肌', '进阶', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1, '手动'),
(4, '减脂入门计划', '减脂', '入门', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 21 DAY), 1, '模板');

-- 插入计划明细
INSERT INTO `plan_detail` (plan_id, day_of_week, exercise_type, sets, reps, duration, note) VALUES
(1, 1, '卧推', 4, 12, 0, '胸肌主项'),
(1, 1, '哑铃飞鸟', 3, 15, 0, '胸肌辅助'),
(1, 1, '绳索下压', 3, 12, 0, '三头'),
(1, 2, '引体向上', 4, 10, 0, '背部主项'),
(1, 2, '划船', 3, 12, 0, '背部辅助'),
(1, 2, '哑铃弯举', 3, 12, 0, '二头'),
(1, 3, '深蹲', 4, 10, 0, '腿部主项'),
(1, 3, '硬拉', 3, 8, 0, '背部链'),
(1, 3, '提踵', 3, 20, 0, '小腿'),
(2, 1, '跑步', 0, 0, 30, '慢跑'),
(2, 2, '瑜伽', 0, 0, 45, '哈他瑜伽'),
(2, 3, '跑步', 0, 0, 35, '间歇跑');

-- 插入社区动态
INSERT INTO `post` (user_id, content, like_count, comment_count, status) VALUES
(3, '今天力量训练状态不错，卧推突破80kg了！', 5, 2, 1),
(4, '坚持跑步一周了，感觉体能明显提升', 3, 1, 1),
(5, 'HIIT真的太累了，但是效果很好，推荐给大家', 7, 3, 1);

-- 插入评论
INSERT INTO `comment` (post_id, user_id, parent_id, content) VALUES
(1, 4, NULL, '厉害！我也要加油'),
(1, 5, NULL, '大佬带带我'),
(2, 3, NULL, '跑友来了，一起加油'),
(3, 3, NULL, '确实，一周三次HIIT效果很好'),
(3, 4, NULL, '对新手友好吗？'),
(3, 5, 5, '建议从入门级开始，慢慢加强度');

-- 插入成就
INSERT INTO `achievement` (user_id, type, achieved_at) VALUES
(3, '7天', DATE_SUB(NOW(), INTERVAL 14 DAY)),
(3, '30天', DATE_SUB(NOW(), INTERVAL 2 DAY));
