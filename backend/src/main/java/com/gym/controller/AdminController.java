package com.gym.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.common.R;
import com.gym.entity.AlertLog;
import com.gym.entity.CheckIn;
import com.gym.entity.Plan;
import com.gym.entity.Post;
import com.gym.entity.User;
import com.gym.mapper.AlertLogMapper;
import com.gym.mapper.CheckInMapper;
import com.gym.mapper.PlanMapper;
import com.gym.mapper.PostMapper;
import com.gym.mapper.UserMapper;
import com.gym.service.AchievementService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final CheckInMapper checkInMapper;
    private final PlanMapper planMapper;
    private final PostMapper postMapper;
    private final AlertLogMapper alertLogMapper;
    private final AchievementService achievementService;

    @GetMapping("/users")
    public R<Object> userList(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) Integer role,
                              @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        if (role != null) qw.eq(User::getRole, role);
        if (status != null) qw.eq(User::getStatus, status);
        qw.orderByDesc(User::getCreatedAt);

        List<User> users = userMapper.selectList(qw);
        List<Map<String, Object>> records = users.stream().skip((long) (page - 1) * size).limit(size).map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("role", u.getRole());
            m.put("fitnessGoal", u.getFitnessGoal());
            m.put("fitnessLevel", u.getFitnessLevel());
            m.put("status", u.getStatus());
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", users.size());
        result.put("current", page);
        result.put("size", size);
        return R.ok(result);
    }

    @PutMapping("/user/{id}/status")
    public R<Void> toggleUserStatus(@PathVariable Long id) {
        User u = userMapper.selectById(id);
        if (u == null) return R.fail("用户不存在");
        u.setStatus(u.getStatus() == 1 ? 0 : 1);
        userMapper.updateById(u);
        return R.ok();
    }

    @GetMapping("/my-users")
    public R<Object> myUsers(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User current = userMapper.selectById(userId);
        if (current == null || current.getRole() < 1) return R.fail("无权限");

        // 督导查看所有普通用户
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getRole, 0).orderByDesc(User::getCreatedAt));

        List<Map<String, Object>> records = users.stream().map(u -> {
            long checkInDays = checkInMapper.selectCount(
                    new LambdaQueryWrapper<CheckIn>().eq(CheckIn::getUserId, u.getId()));
            int streak = achievementService.calculateStreak(u.getId());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("fitnessGoal", u.getFitnessGoal());
            m.put("fitnessLevel", u.getFitnessLevel());
            m.put("status", u.getStatus());
            m.put("checkInDays", (int) checkInDays);
            m.put("streak", streak);
            m.put("needsRemind", streak == 0);
            return m;
        }).collect(Collectors.toList());

        return R.ok(records);
    }

    @GetMapping("/dashboard")
    public R<Object> dashboard(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User currentUser = userMapper.selectById(userId);
        if (currentUser == null || currentUser.getRole() < 1) return R.fail("无权限");

        List<User> allUsers = userMapper.selectList(null);
        List<CheckIn> allCheckIns = checkInMapper.selectList(null);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalUsers", (int) allUsers.stream().filter(u -> u.getRole() == 0).count());
        data.put("todayCheckIns", (int) allCheckIns.stream()
                .filter(c -> c.getCheckInTime().toLocalDate().equals(LocalDate.now())).count());
        data.put("activePlans", planMapper.selectCount(
                new LambdaQueryWrapper<Plan>().eq(Plan::getStatus, 1).eq(Plan::getIsTemplate, 0)).intValue());

        // 异常提醒统计
        long anomalyCount = alertLogMapper.selectCount(
                new LambdaQueryWrapper<AlertLog>().eq(AlertLog::getType, "anomaly"));
        data.put("anomalyCount", (int) anomalyCount);

        // 用户打卡率
        List<Map<String, Object>> userRates = new ArrayList<>();
        for (User u : allUsers) {
            if (u.getRole() != 0) continue;
            long checkInDays = allCheckIns.stream()
                    .filter(c -> c.getUserId().equals(u.getId()))
                    .map(c -> c.getCheckInTime().toLocalDate()).distinct().count();
            int streak = achievementService.calculateStreak(u.getId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", u.getId());
            item.put("username", u.getUsername());
            item.put("checkInDays", (int) checkInDays);
            item.put("streak", streak);
            item.put("fitnessGoal", u.getFitnessGoal());
            item.put("hasPlan", planMapper.selectCount(
                    new LambdaQueryWrapper<Plan>().eq(Plan::getUserId, u.getId()).eq(Plan::getStatus, 1)) > 0);
            item.put("needsRemind", streak == 0);
            userRates.add(item);
        }
        data.put("userRates", userRates);
        return R.ok(data);
    }

    @GetMapping("/alerts")
    public R<Object> alerts(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "20") int size,
                             @RequestParam(required = false) String type) {
        LambdaQueryWrapper<AlertLog> qw = new LambdaQueryWrapper<>();
        if (type != null) qw.eq(AlertLog::getType, type);
        qw.orderByDesc(AlertLog::getCreatedAt);

        List<AlertLog> logs = alertLogMapper.selectList(qw);
        List<Map<String, Object>> records = logs.stream().skip((long) (page - 1) * size).limit(size).map(log -> {
            User u = userMapper.selectById(log.getUserId());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", log.getId());
            m.put("userId", log.getUserId());
            m.put("username", u != null ? u.getUsername() : "未知");
            m.put("type", log.getType());
            m.put("message", log.getMessage());
            m.put("createdAt", log.getCreatedAt());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", logs.size());
        return R.ok(result);
    }

    @PostMapping("/remind")
    public R<Void> sendRemind(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long senderId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<Integer> userIdsRaw = (List<Integer>) body.get("userIds");
        String message = (String) body.getOrDefault("message", "请记得今日打卡哦！");

        if (userIdsRaw == null || userIdsRaw.isEmpty()) return R.fail("请选择要提醒的用户");
        if (message.length() > 200) return R.fail("提醒内容不能超过200字");

        for (Integer uid : userIdsRaw) {
            AlertLog log = new AlertLog();
            log.setUserId(uid.longValue());
            log.setType("remind");
            log.setMessage(message);
            alertLogMapper.insert(log);
        }
        return R.ok();
    }

    @GetMapping("/posts")
    public R<Object> posts(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "20") int size) {
        LambdaQueryWrapper<Post> qw = new LambdaQueryWrapper<>();
        qw.orderByDesc(Post::getCreatedAt);
        List<Post> all = postMapper.selectList(qw);

        List<Map<String, Object>> records = all.stream().skip((long) (page - 1) * size).limit(size).map(p -> {
            User u = userMapper.selectById(p.getUserId());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("userId", p.getUserId());
            m.put("username", u != null ? u.getUsername() : "未知");
            m.put("content", p.getContent().length() > 100 ? p.getContent().substring(0, 100) + "..." : p.getContent());
            m.put("likeCount", p.getLikeCount());
            m.put("commentCount", p.getCommentCount());
            m.put("status", p.getStatus());
            m.put("createdAt", p.getCreatedAt());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", records);
        result.put("total", all.size());
        return R.ok(result);
    }

    @DeleteMapping("/post/{id}")
    public R<Void> deletePost(@PathVariable Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) return R.fail("动态不存在");
        post.setStatus(0); // 0=违规删除
        postMapper.updateById(post);
        return R.ok();
    }
}
