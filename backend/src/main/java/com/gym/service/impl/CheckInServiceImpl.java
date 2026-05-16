package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.dto.CheckInDTO;
import com.gym.dto.CheckInQuery;
import com.gym.entity.CheckIn;
import com.gym.mapper.CheckInMapper;
import com.gym.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckInServiceImpl implements CheckInService {

    private final CheckInMapper checkInMapper;

    @Override
    public CheckIn create(Long userId, CheckInDTO dto) {
        // 每日上限校验
        int todayCount = getTodayCount(userId);
        if (todayCount >= 3) {
            throw new RuntimeException("今日打卡次数已达上限（3次）");
        }

        CheckIn entity = new CheckIn();
        entity.setUserId(userId);
        entity.setExerciseType(dto.getExerciseType());
        entity.setDurationMinutes(dto.getDurationMinutes());
        entity.setCalories(dto.getCalories() != null ? dto.getCalories() : 0);
        entity.setNote(dto.getNote());
        entity.setCheckInTime(LocalDateTime.now());
        checkInMapper.insert(entity);
        return entity;
    }

    @Override
    public Page<CheckIn> list(Long userId, CheckInQuery query) {
        Page<CheckIn> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<CheckIn> wrapper = new LambdaQueryWrapper<CheckIn>()
                .eq(CheckIn::getUserId, userId)
                .orderByDesc(CheckIn::getCheckInTime);

        if (query.getExerciseType() != null && !query.getExerciseType().isEmpty()) {
            wrapper.eq(CheckIn::getExerciseType, query.getExerciseType());
        }
        if (query.getStartDate() != null) {
            wrapper.ge(CheckIn::getCheckInTime, LocalDate.parse(query.getStartDate()).atStartOfDay());
        }
        if (query.getEndDate() != null) {
            wrapper.le(CheckIn::getCheckInTime, LocalDate.parse(query.getEndDate()).plusDays(1).atStartOfDay());
        }

        return checkInMapper.selectPage(page, wrapper);
    }

    @Override
    public Map<String, Integer> getCalendar(Long userId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().plusDays(1).atStartOfDay();

        List<CheckIn> records = checkInMapper.selectList(
                new LambdaQueryWrapper<CheckIn>()
                        .eq(CheckIn::getUserId, userId)
                        .ge(CheckIn::getCheckInTime, start)
                        .lt(CheckIn::getCheckInTime, end));

        Map<String, Integer> calendar = new LinkedHashMap<>();
        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            String key = String.format("%d-%02d-%02d", year, month, day);
            LocalDate date = LocalDate.of(year, month, day);
            long count = records.stream()
                    .filter(r -> r.getCheckInTime().toLocalDate().equals(date))
                    .count();
            calendar.put(key, (int) count);
        }
        return calendar;
    }

    @Override
    public int getTodayCount(Long userId) {
        LocalDate today = LocalDate.now();
        return checkInMapper.selectCount(
                new LambdaQueryWrapper<CheckIn>()
                        .eq(CheckIn::getUserId, userId)
                        .ge(CheckIn::getCheckInTime, today.atStartOfDay())
                        .lt(CheckIn::getCheckInTime, today.plusDays(1).atStartOfDay())).intValue();
    }

    @Override
    public byte[] export(Long userId, String format) {
        List<CheckIn> records = checkInMapper.selectList(
                new LambdaQueryWrapper<CheckIn>()
                        .eq(CheckIn::getUserId, userId)
                        .orderByDesc(CheckIn::getCheckInTime));

        StringBuilder sb = new StringBuilder();
        sb.append("运动类型,时长(分钟),消耗卡路里,备注,打卡时间\n");
        for (CheckIn c : records) {
            sb.append(String.format("%s,%d,%d,%s,%s\n",
                    c.getExerciseType(),
                    c.getDurationMinutes(),
                    c.getCalories(),
                    c.getNote() != null ? c.getNote() : "",
                    c.getCheckInTime()));
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
