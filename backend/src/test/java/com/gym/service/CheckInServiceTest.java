package com.gym.service;

import com.gym.dto.CheckInDTO;
import com.gym.dto.CheckInQuery;
import com.gym.mapper.CheckInMapper;
import com.gym.entity.CheckIn;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CheckInServiceTest {

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private CheckInMapper checkInMapper;

    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        checkInMapper.delete(new LambdaQueryWrapper<CheckIn>().eq(CheckIn::getUserId, userId));
    }

    @Test
    void shouldCreateCheckIn() {
        CheckInDTO dto = new CheckInDTO();
        dto.setExerciseType("跑步");
        dto.setDurationMinutes(30);
        dto.setCalories(250);
        dto.setNote("晨跑");

        CheckIn result = checkInService.create(userId, dto);
        assertNotNull(result.getId());
        assertEquals("跑步", result.getExerciseType());
        assertEquals(userId, result.getUserId());
    }

    @Test
    void shouldGetTodayCount() {
        // 先创建两次打卡
        CheckInDTO dto = new CheckInDTO();
        dto.setExerciseType("跑步");
        dto.setDurationMinutes(20);
        dto.setCalories(100);
        checkInService.create(userId, dto);
        checkInService.create(userId, dto);

        int count = checkInService.getTodayCount(userId);
        assertEquals(2, count);
    }

    @Test
    void shouldRejectWhenDailyLimitReached() {
        CheckInDTO dto = new CheckInDTO();
        dto.setExerciseType("跑步");
        dto.setDurationMinutes(20);
        dto.setCalories(100);

        checkInService.create(userId, dto);
        checkInService.create(userId, dto);
        checkInService.create(userId, dto);

        assertThrows(RuntimeException.class, () -> checkInService.create(userId, dto));
    }

    @Test
    void shouldQueryPaginatedList() {
        CheckInDTO dto = new CheckInDTO();
        dto.setExerciseType("力量训练");
        dto.setDurationMinutes(45);
        dto.setCalories(300);
        checkInService.create(userId, dto);

        CheckInQuery query = new CheckInQuery();
        query.setPage(1);
        query.setSize(10);

        var page = checkInService.list(userId, query);
        assertTrue(page.getRecords().size() > 0);
    }

    @Test
    void shouldGetCalendarData() {
        CheckInDTO dto = new CheckInDTO();
        dto.setExerciseType("游泳");
        dto.setDurationMinutes(30);
        dto.setCalories(200);
        checkInService.create(userId, dto);

        var calendar = checkInService.getCalendar(userId, 2026, 5);
        assertNotNull(calendar);
    }
}
