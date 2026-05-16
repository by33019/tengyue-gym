package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.dto.PlanCreateDTO;
import com.gym.dto.PlanDetailDTO;
import com.gym.dto.PlanVO;
import com.gym.entity.Plan;
import com.gym.entity.PlanDetail;
import com.gym.entity.User;
import com.gym.mapper.PlanMapper;
import com.gym.mapper.PlanDetailMapper;
import com.gym.mapper.UserMapper;
import com.gym.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanMapper planMapper;
    private final PlanDetailMapper planDetailMapper;
    private final UserMapper userMapper;

    private static final Map<String, Integer> LEVEL_ORDER = Map.of("入门", 1, "进阶", 2, "高级", 3);

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
        plan.setIsTemplate(0);
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
        List<PlanDetailDTO> savedDetails = dto.getDetails();
        return toVO(plan, savedDetails);
    }

    @Override
    public PlanVO getById(Long userId, Long planId) {
        Plan plan = planMapper.selectById(planId);
        if (plan == null || (!plan.getUserId().equals(userId) && plan.getIsTemplate() == 0)) {
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
                .eq(Plan::getIsTemplate, 0)
                .orderByDesc(Plan::getCreatedAt);
        if (status != null) wrapper.eq(Plan::getStatus, status);
        Page<Plan> result = planMapper.selectPage(page, wrapper);
        return mapPage(result, pageNum, size);
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
        if (plan == null || !plan.getUserId().equals(userId)) throw new RuntimeException("计划不存在或无权访问");
        plan.setStatus(plan.getStatus() == 1 ? 0 : 1);
        planMapper.updateById(plan);
    }

    @Override
    public Page<PlanVO> listTemplates(int pageNum, int size) {
        Page<Plan> page = new Page<>(pageNum, size);
        LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<Plan>()
                .eq(Plan::getIsTemplate, 1)
                .eq(Plan::getStatus, 1)
                .orderByDesc(Plan::getCreatedAt);
        return mapPage(planMapper.selectPage(page, wrapper), pageNum, size);
    }

    @Override
    public void publishTemplate(Long userId, Long planId) {
        User u = userMapper.selectById(userId);
        if (u == null || u.getRole() < 1) throw new RuntimeException("仅督导和管理员可发布模板");
        Plan plan = planMapper.selectById(planId);
        if (plan == null) throw new RuntimeException("计划不存在");
        plan.setIsTemplate(1);
        plan.setSource("模板");
        planMapper.updateById(plan);
    }

    @Override
    public void unpublishTemplate(Long userId, Long planId) {
        User u = userMapper.selectById(userId);
        if (u == null || u.getRole() < 1) throw new RuntimeException("仅督导和管理员可取消发布");
        Plan plan = planMapper.selectById(planId);
        if (plan == null) throw new RuntimeException("计划不存在");
        plan.setIsTemplate(0);
        planMapper.updateById(plan);
    }

    @Override
    @Transactional
    public PlanVO applyTemplate(Long userId, Long templateId) {
        Plan template = planMapper.selectById(templateId);
        if (template == null || template.getIsTemplate() == 0) throw new RuntimeException("模板不存在");

        User user = userMapper.selectById(userId);
        String adjustedDifficulty = adjustDifficulty(template.getDifficulty(), user.getFitnessLevel());

        List<PlanDetail> templateDetails = planDetailMapper.selectList(
                new LambdaQueryWrapper<PlanDetail>().eq(PlanDetail::getPlanId, templateId));

        Plan plan = new Plan();
        plan.setUserId(userId);
        plan.setPlanName(template.getPlanName() + "（我的）");
        plan.setGoal(template.getGoal());
        plan.setDifficulty(adjustedDifficulty);
        plan.setStartDate(LocalDate.now());
        plan.setEndDate(LocalDate.now().plusDays(30));
        plan.setStatus(1);
        plan.setSource("模板");
        plan.setIsTemplate(0);
        planMapper.insert(plan);

        for (PlanDetail td : templateDetails) {
            PlanDetail d = new PlanDetail();
            d.setPlanId(plan.getId());
            d.setDayOfWeek(td.getDayOfWeek());
            d.setExerciseType(td.getExerciseType());
            d.setSets(adjustByDifficulty(adjustedDifficulty, td.getSets(), template.getDifficulty()));
            d.setReps(adjustByDifficulty(adjustedDifficulty, td.getReps(), template.getDifficulty()));
            d.setDuration(td.getDuration());
            d.setNote(td.getNote());
            planDetailMapper.insert(d);
        }

        List<PlanDetailDTO> dtos = planDetailMapper.selectList(
                new LambdaQueryWrapper<PlanDetail>().eq(PlanDetail::getPlanId, plan.getId()))
                .stream().map(this::toDetailDTO).collect(Collectors.toList());
        return toVO(plan, dtos);
    }

    private String adjustDifficulty(String templateDiff, String userLevel) {
        if (userLevel == null) return templateDiff;
        int tl = LEVEL_ORDER.getOrDefault(templateDiff, 2);
        int ul = LEVEL_ORDER.getOrDefault(userLevel, 2);
        if (ul < tl) return "入门";
        if (ul > tl) return "高级";
        return templateDiff;
    }

    private int adjustByDifficulty(String difficulty, int value, String original) {
        if (value == 0) return 0;
        if (difficulty.equals(original)) return value;
        if ("入门".equals(difficulty)) return Math.max(1, value - 1);
        if ("高级".equals(difficulty)) return value + 1;
        return value;
    }

    private Page<PlanVO> mapPage(Page<Plan> result, int pageNum, int size) {
        Page<PlanVO> voPage = new Page<>(pageNum, size, result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(p -> toVO(p, List.of())).collect(Collectors.toList()));
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
