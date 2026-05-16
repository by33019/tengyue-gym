package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.dto.PlanCreateDTO;
import com.gym.dto.PlanDetailDTO;
import com.gym.dto.PlanVO;
import com.gym.entity.Plan;
import com.gym.entity.PlanDetail;
import com.gym.mapper.PlanMapper;
import com.gym.mapper.PlanDetailMapper;
import com.gym.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanMapper planMapper;
    private final PlanDetailMapper planDetailMapper;

    @Override
    @Transactional
    public PlanVO create(Long userId, PlanCreateDTO dto) {
        Plan plan = new Plan();
        plan.setUserId(userId);
        plan.setPlanName(dto.getPlanName());
        plan.setGoal(dto.getGoal());
        plan.setDifficulty(dto.getDifficulty());
        plan.setStartDate(LocalDate.parse(dto.getStartDate()));
        plan.setEndDate(LocalDate.parse(dto.getEndDate()));
        plan.setStatus(1);
        plan.setSource(dto.getSource() != null ? dto.getSource() : "手动");
        planMapper.insert(plan);

        if (dto.getDetails() != null) {
            for (PlanDetailDTO dd : dto.getDetails()) {
                PlanDetail detail = new PlanDetail();
                detail.setPlanId(plan.getId());
                detail.setDayOfWeek(dd.getDayOfWeek());
                detail.setExerciseType(dd.getExerciseType());
                detail.setSets(dd.getSets() != null ? dd.getSets() : 3);
                detail.setReps(dd.getReps() != null ? dd.getReps() : 12);
                detail.setDuration(dd.getDuration() != null ? dd.getDuration() : 0);
                detail.setNote(dd.getNote());
                planDetailMapper.insert(detail);
            }
        }
        return toVO(plan, dto.getDetails());
    }

    @Override
    public PlanVO getById(Long userId, Long planId) {
        Plan plan = planMapper.selectById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new RuntimeException("计划不存在或无权访问");
        }
        List<PlanDetail> details = planDetailMapper.selectList(
                new LambdaQueryWrapper<PlanDetail>().eq(PlanDetail::getPlanId, planId));
        return toVO(plan, details.stream().map(this::toDetailDTO).collect(Collectors.toList()));
    }

    @Override
    public Page<PlanVO> list(Long userId, int pageNum, int size, Integer status) {
        Page<Plan> page = new Page<>(pageNum, size);
        LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<Plan>()
                .eq(Plan::getUserId, userId)
                .orderByDesc(Plan::getCreatedAt);
        if (status != null) {
            wrapper.eq(Plan::getStatus, status);
        }
        Page<Plan> result = planMapper.selectPage(page, wrapper);
        Page<PlanVO> voPage = new Page<>(pageNum, size, result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(p -> toVO(p, List.of()))
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    @Transactional
    public void update(Long userId, Long planId, PlanCreateDTO dto) {
        Plan plan = planMapper.selectById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new RuntimeException("计划不存在或无权访问");
        }
        plan.setPlanName(dto.getPlanName());
        plan.setGoal(dto.getGoal());
        plan.setDifficulty(dto.getDifficulty());
        plan.setStartDate(LocalDate.parse(dto.getStartDate()));
        plan.setEndDate(LocalDate.parse(dto.getEndDate()));
        planMapper.updateById(plan);

        // 删除旧明细，重建
        planDetailMapper.delete(new LambdaQueryWrapper<PlanDetail>().eq(PlanDetail::getPlanId, planId));
        if (dto.getDetails() != null) {
            for (PlanDetailDTO dd : dto.getDetails()) {
                PlanDetail detail = new PlanDetail();
                detail.setPlanId(planId);
                detail.setDayOfWeek(dd.getDayOfWeek());
                detail.setExerciseType(dd.getExerciseType());
                detail.setSets(dd.getSets() != null ? dd.getSets() : 3);
                detail.setReps(dd.getReps() != null ? dd.getReps() : 12);
                detail.setDuration(dd.getDuration() != null ? dd.getDuration() : 0);
                detail.setNote(dd.getNote());
                planDetailMapper.insert(detail);
            }
        }
    }

    @Override
    public void toggleStatus(Long userId, Long planId) {
        Plan plan = planMapper.selectById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new RuntimeException("计划不存在或无权访问");
        }
        plan.setStatus(plan.getStatus() == 1 ? 0 : 1);
        planMapper.updateById(plan);
    }

    @Override
    public Page<PlanVO> listTemplates(int pageNum, int size) {
        Page<Plan> page = new Page<>(pageNum, size);
        LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<Plan>()
                .eq(Plan::getSource, "模板")
                .eq(Plan::getStatus, 1)
                .orderByDesc(Plan::getCreatedAt);
        Page<Plan> result = planMapper.selectPage(page, wrapper);
        Page<PlanVO> voPage = new Page<>(pageNum, size, result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(p -> toVO(p, List.of()))
                .collect(Collectors.toList()));
        return voPage;
    }

    private PlanVO toVO(Plan plan, List<PlanDetailDTO> details) {
        PlanVO vo = new PlanVO();
        vo.setId(plan.getId());
        vo.setPlanName(plan.getPlanName());
        vo.setGoal(plan.getGoal());
        vo.setDifficulty(plan.getDifficulty());
        vo.setStartDate(plan.getStartDate().toString());
        vo.setEndDate(plan.getEndDate().toString());
        vo.setStatus(plan.getStatus());
        vo.setSource(plan.getSource());
        vo.setDetails(details);
        return vo;
    }

    private PlanDetailDTO toDetailDTO(PlanDetail d) {
        PlanDetailDTO dd = new PlanDetailDTO();
        dd.setId(d.getId());
        dd.setDayOfWeek(d.getDayOfWeek());
        dd.setExerciseType(d.getExerciseType());
        dd.setSets(d.getSets());
        dd.setReps(d.getReps());
        dd.setDuration(d.getDuration());
        dd.setNote(d.getNote());
        return dd;
    }
}
