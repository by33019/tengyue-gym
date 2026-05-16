package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.entity.CheckIn;
import com.gym.entity.User;
import com.gym.mapper.CheckInMapper;
import com.gym.mapper.UserMapper;
import com.gym.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final CheckInMapper checkInMapper;
    private final UserMapper userMapper;

    @Override
    public Map<String, Object> getSummary(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);

        List<CheckIn> all = checkInMapper.selectList(
                new LambdaQueryWrapper<CheckIn>().eq(CheckIn::getUserId, userId));
        List<CheckIn> thisWeek = all.stream()
                .filter(c -> c.getCheckInTime().toLocalDate().isAfter(weekAgo) || c.getCheckInTime().toLocalDate().isEqual(weekAgo))
                .collect(Collectors.toList());

        int totalMinutes = all.stream().mapToInt(CheckIn::getDurationMinutes).sum();
        int totalCalories = all.stream().mapToInt(CheckIn::getCalories).sum();
        long totalDays = all.stream().map(c -> c.getCheckInTime().toLocalDate()).distinct().count();
        int thisWeekMinutes = thisWeek.stream().mapToInt(CheckIn::getDurationMinutes).sum();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalMinutes", totalMinutes);
        summary.put("totalCalories", totalCalories);
        summary.put("totalDays", (int) totalDays);
        summary.put("avgMinutes", totalDays > 0 ? totalMinutes / totalDays : 0);
        summary.put("thisWeekMinutes", thisWeekMinutes);
        return summary;
    }

    @Override
    public Map<String, Object> getTrend(Long userId, String period) {
        int days = "month".equals(period) ? 30 : 7;
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1);

        Map<String, Integer> daily = new LinkedHashMap<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = start.plusDays(i);
            daily.put(d.toString(), 0);
        }

        List<CheckIn> records = checkInMapper.selectList(
                new LambdaQueryWrapper<CheckIn>()
                        .eq(CheckIn::getUserId, userId)
                        .ge(CheckIn::getCheckInTime, start.atStartOfDay()));

        for (CheckIn c : records) {
            String key = c.getCheckInTime().toLocalDate().toString();
            daily.merge(key, c.getDurationMinutes(), Integer::sum);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", new ArrayList<>(daily.keySet()));
        result.put("values", new ArrayList<>(daily.values()));
        return result;
    }

    @Override
    public List<Map<String, Object>> getRanking(String type, String period) {
        List<CheckIn> all = checkInMapper.selectList(null);
        int days = "month".equals(period) ? 30 : 7;
        LocalDate since = LocalDate.now().minusDays(days);

        Map<Long, List<CheckIn>> grouped = all.stream()
                .filter(c -> c.getCheckInTime().toLocalDate().isAfter(since))
                .collect(Collectors.groupingBy(CheckIn::getUserId));

        Map<Long, User> userCache = new HashMap<>();
        List<User> users = userMapper.selectList(null);
        for (User u : users) userCache.put(u.getId(), u);

        return grouped.entrySet().stream().map(e -> {
            Map<String, Object> item = new LinkedHashMap<>();
            User u = userCache.get(e.getKey());
            item.put("userId", e.getKey());
            item.put("username", u != null && u.getIsAnonymous() != null && u.getIsAnonymous() == 0 ? u.getUsername() : null);
            if ("calories".equals(type)) {
                item.put("value", e.getValue().stream().mapToInt(CheckIn::getCalories).sum());
            } else {
                item.put("value", (int) e.getValue().stream().map(CheckIn::getCheckInTime).map(LocalDate::from).distinct().count());
            }
            return item;
        }).sorted((a, b) -> ((Integer) b.get("value")).compareTo((Integer) a.get("value")))
                .limit(20)
                .collect(Collectors.toList());
    }
}
