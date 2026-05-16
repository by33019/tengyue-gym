package com.gym.mapper;

import com.gym.entity.Plan;
import com.gym.entity.PlanDetail;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class PlanMapperTest {

    @Autowired
    private PlanMapper planMapper;

    @Autowired
    private PlanDetailMapper planDetailMapper;

    @Test
    void shouldInsertAndQueryPlan() {
        Plan plan = new Plan();
        plan.setUserId(1L);
        plan.setPlanName("增肌训练");
        plan.setGoal("增肌");
        plan.setDifficulty("进阶");
        plan.setStartDate(LocalDate.now());
        plan.setEndDate(LocalDate.now().plusDays(30));
        plan.setStatus(1);
        plan.setSource("手动");

        planMapper.insert(plan);
        assertNotNull(plan.getId());

        Plan found = planMapper.selectById(plan.getId());
        assertEquals("增肌训练", found.getPlanName());
        assertEquals(1L, found.getUserId());
    }

    @Test
    void shouldQueryPlansByUserId() {
        Plan p1 = new Plan(); p1.setUserId(5L); p1.setPlanName("A计划");
        p1.setGoal("增肌"); p1.setDifficulty("入门");
        p1.setStartDate(LocalDate.now()); p1.setEndDate(LocalDate.now().plusDays(7));
        p1.setStatus(1); p1.setSource("手动");
        planMapper.insert(p1);

        Plan p2 = new Plan(); p2.setUserId(5L); p2.setPlanName("B计划");
        p2.setGoal("减脂"); p2.setDifficulty("进阶");
        p2.setStartDate(LocalDate.now()); p2.setEndDate(LocalDate.now().plusDays(14));
        p2.setStatus(0); p2.setSource("AI");
        planMapper.insert(p2);

        List<Plan> list = planMapper.selectList(
                new LambdaQueryWrapper<Plan>()
                        .eq(Plan::getUserId, 5L)
                        .eq(Plan::getStatus, 1));
        assertEquals(1, list.size());
        assertEquals("A计划", list.get(0).getPlanName());
    }

    @Test
    void shouldInsertAndQueryPlanDetails() {
        Plan plan = new Plan(); plan.setUserId(1L); plan.setPlanName("测试");
        plan.setGoal("塑形"); plan.setDifficulty("入门");
        plan.setStartDate(LocalDate.now()); plan.setEndDate(LocalDate.now().plusDays(7));
        plan.setStatus(1); plan.setSource("模板");
        planMapper.insert(plan);

        PlanDetail d1 = new PlanDetail(); d1.setPlanId(plan.getId());
        d1.setDayOfWeek(1); d1.setExerciseType("深蹲");
        d1.setSets(4); d1.setReps(12); d1.setDuration(0);
        planDetailMapper.insert(d1);

        PlanDetail d2 = new PlanDetail(); d2.setPlanId(plan.getId());
        d2.setDayOfWeek(1); d2.setExerciseType("卧推");
        d2.setSets(3); d2.setReps(10); d2.setDuration(0);
        planDetailMapper.insert(d2);

        List<PlanDetail> details = planDetailMapper.selectList(
                new LambdaQueryWrapper<PlanDetail>()
                        .eq(PlanDetail::getPlanId, plan.getId()));
        assertEquals(2, details.size());
    }
}
