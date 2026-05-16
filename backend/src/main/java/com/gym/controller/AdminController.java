package com.gym.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.common.R;
import com.gym.entity.CheckIn;
import com.gym.entity.Plan;
import com.gym.entity.User;
import com.gym.mapper.CheckInMapper;
import com.gym.mapper.PlanMapper;
import com.gym.mapper.UserMapper;
import com.gym.service.AchievementService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final CheckInMapper checkInMapper;
    private final PlanMapper planMapper;
    private final AchievementService achievementService;

    @GetMapping("/users")
    public R<Object> userList(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size) {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreatedAt));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("records", users.stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("role", u.getRole());
            m.put("fitnessGoal", u.getFitnessGoal());
            m.put("fitnessLevel", u.getFitnessLevel());
            m.put("status", u.getStatus());
            return m;
        }).skip((long) (page - 1) * size).limit(size).collect(Collectors.toList()));
        result.put("total", users.size());
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

    @PostMapping("/remind")
    public R<Void> sendRemind(HttpServletRequest request) {
        // 督导批量提醒功能（记录提醒日志）
        return R.ok();
    }
}
