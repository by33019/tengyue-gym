package com.gym.service;

import com.gym.dto.PlanCreateDTO;
import com.gym.dto.PlanDetailDTO;
import com.gym.mapper.PlanMapper;
import com.gym.mapper.PlanDetailMapper;
import com.gym.entity.Plan;
import com.gym.entity.PlanDetail;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PlanServiceTest {

    @Autowired private PlanService planService;
    @Autowired private PlanMapper planMapper;
    @Autowired private PlanDetailMapper planDetailMapper;

    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        planDetailMapper.delete(new LambdaQueryWrapper<PlanDetail>().isNotNull(PlanDetail::getId));
        planMapper.delete(new LambdaQueryWrapper<Plan>().eq(Plan::getUserId, userId));
    }

    PlanCreateDTO buildDTO() {
        PlanCreateDTO dto = new PlanCreateDTO();
        dto.setPlanName("测试三分化");
        dto.setGoal("增肌");
        dto.setDifficulty("进阶");
        dto.setStartDate("2026-06-01");
        dto.setEndDate("2026-06-30");
        dto.setSource("手动");
        dto.setDetails(List.of(
                detailDTO(1, "卧推", 4, 12),
                detailDTO(1, "飞鸟", 3, 15),
                detailDTO(2, "深蹲", 4, 10)
        ));
        return dto;
    }

    PlanDetailDTO detailDTO(int day, String type, int sets, int reps) {
        PlanDetailDTO d = new PlanDetailDTO();
        d.setDayOfWeek(day); d.setExerciseType(type);
        d.setSets(sets); d.setReps(reps);
        return d;
    }

    @Test
    void shouldCreatePlanWithDetails() {
        var vo = planService.create(userId, buildDTO());
        assertNotNull(vo.getId());
        assertEquals("测试三分化", vo.getPlanName());
        assertEquals(3, vo.getDetails().size());
    }

    @Test
    void shouldListUserPlans() {
        planService.create(userId, buildDTO());
        var page = planService.list(userId, 1, 10, null);
        assertEquals(1, page.getRecords().size());
    }

    @Test
    void shouldFilterPlansByStatus() {
        planService.create(userId, buildDTO());

        PlanCreateDTO dto2 = buildDTO();
        dto2.setPlanName("已停用计划");
        var vo = planService.create(userId, dto2);
        planService.toggleStatus(userId, vo.getId());

        var active = planService.list(userId, 1, 10, 1);
        assertEquals(1, active.getRecords().size());
        assertEquals("测试三分化", active.getRecords().get(0).getPlanName());
    }

    @Test
    void shouldGetPlanDetail() {
        var created = planService.create(userId, buildDTO());
        var vo = planService.getById(userId, created.getId());
        assertNotNull(vo);
        assertEquals(3, vo.getDetails().size());
    }

    @Test
    void shouldNotAccessOtherUserPlan() {
        var created = planService.create(userId, buildDTO());
        assertThrows(RuntimeException.class, () -> planService.getById(999L, created.getId()));
    }

    @Test
    void shouldUpdatePlan() {
        var created = planService.create(userId, buildDTO());

        PlanCreateDTO update = buildDTO();
        update.setPlanName("修改后的计划");
        update.setDetails(List.of(detailDTO(1, "跑步", 0, 0)));

        planService.update(userId, created.getId(), update);
        var vo = planService.getById(userId, created.getId());
        assertEquals("修改后的计划", vo.getPlanName());
        assertEquals(1, vo.getDetails().size());
    }
}
