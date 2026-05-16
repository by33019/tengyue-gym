package com.gym.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.entity.Achievement;
import com.gym.entity.CheckIn;
import com.gym.mapper.AchievementMapper;
import com.gym.mapper.CheckInMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementMapper achievementMapper;
    private final CheckInMapper checkInMapper;

    public void checkAndAward(Long userId) {
        int streak = calculateStreak(userId);
        String type = null;
        if (streak >= 100) type = "100天";
        else if (streak >= 30) type = "30天";
        else if (streak >= 7) type = "7天";
        if (type == null) return;

        Long count = achievementMapper.selectCount(
                new LambdaQueryWrapper<Achievement>()
                        .eq(Achievement::getUserId, userId)
                        .eq(Achievement::getType, type));
        if (count == 0) {
            Achievement a = new Achievement();
            a.setUserId(userId);
            a.setType(type);
            a.setAchievedAt(LocalDateTime.now());
            achievementMapper.insert(a);
        }
    }

    public List<Map<String, Object>> getUserAchievements(Long userId) {
        List<Achievement> list = achievementMapper.selectList(
                new LambdaQueryWrapper<Achievement>()
                        .eq(Achievement::getUserId, userId)
                        .orderByAsc(Achievement::getAchievedAt));

        return list.stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("type", a.getType());
            m.put("achievedAt", a.getAchievedAt());
            return m;
        }).collect(Collectors.toList());
    }

    public int calculateStreak(Long userId) {
        List<CheckIn> records = checkInMapper.selectList(
                new LambdaQueryWrapper<CheckIn>()
                        .eq(CheckIn::getUserId, userId)
                        .orderByDesc(CheckIn::getCheckInTime));

        if (records.isEmpty()) return 0;

        Set<LocalDate> days = records.stream()
                .map(c -> c.getCheckInTime().toLocalDate())
                .collect(Collectors.toSet());

        int streak = 0;
        LocalDate today = LocalDate.now();
        // 今天或昨天必须有打卡才能开始计算连续
        if (!days.contains(today) && !days.contains(today.minusDays(1))) return 0;

        LocalDate check = days.contains(today) ? today : today.minusDays(1);
        while (days.contains(check)) {
            streak++;
            check = check.minusDays(1);
        }
        return streak;
    }
}
