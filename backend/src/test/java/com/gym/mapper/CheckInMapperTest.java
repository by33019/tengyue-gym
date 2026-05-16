package com.gym.mapper;

import com.gym.entity.CheckIn;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CheckInMapperTest {

    @Autowired
    private CheckInMapper checkInMapper;

    @Test
    void shouldInsertAndFindCheckIn() {
        CheckIn c = new CheckIn();
        c.setUserId(1L);
        c.setExerciseType("跑步");
        c.setDurationMinutes(30);
        c.setCalories(250);
        c.setCheckInTime(LocalDateTime.now());

        checkInMapper.insert(c);
        assertNotNull(c.getId());

        CheckIn found = checkInMapper.selectById(c.getId());
        assertNotNull(found);
        assertEquals("跑步", found.getExerciseType());
        assertEquals(30, found.getDurationMinutes());
    }

    @Test
    void shouldCountTodayCheckInsByUserId() {
        Long userId = 2L;
        LocalDateTime now = LocalDateTime.now();
        CheckIn c1 = new CheckIn(); c1.setUserId(userId); c1.setExerciseType("跑步");
        c1.setDurationMinutes(20); c1.setCalories(100); c1.setCheckInTime(now);
        checkInMapper.insert(c1);

        CheckIn c2 = new CheckIn(); c2.setUserId(userId); c2.setExerciseType("瑜伽");
        c2.setDurationMinutes(30); c2.setCalories(120); c2.setCheckInTime(now);
        checkInMapper.insert(c2);

        Long count = checkInMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckIn>()
                        .eq(CheckIn::getUserId, userId)
                        .ge(CheckIn::getCheckInTime, now.toLocalDate().atStartOfDay())
                        .le(CheckIn::getCheckInTime, now.toLocalDate().plusDays(1).atStartOfDay()));

        assertEquals(2L, count);
    }

    @Test
    void shouldQueryCheckInsByDateRange() {
        Long userId = 3L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime twoDaysAgo = now.minusDays(2);

        CheckIn c1 = new CheckIn(); c1.setUserId(userId); c1.setExerciseType("游泳");
        c1.setDurationMinutes(40); c1.setCalories(300); c1.setCheckInTime(yesterday);
        checkInMapper.insert(c1);

        CheckIn c2 = new CheckIn(); c2.setUserId(userId); c2.setExerciseType("力量训练");
        c2.setDurationMinutes(60); c2.setCalories(500); c2.setCheckInTime(twoDaysAgo);
        checkInMapper.insert(c2);

        List<CheckIn> list = checkInMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CheckIn>()
                        .eq(CheckIn::getUserId, userId)
                        .ge(CheckIn::getCheckInTime, twoDaysAgo.toLocalDate().atStartOfDay())
                        .le(CheckIn::getCheckInTime, now.toLocalDate().plusDays(1).atStartOfDay())
                        .orderByDesc(CheckIn::getCheckInTime));

        assertTrue(list.size() >= 2);
    }
}
