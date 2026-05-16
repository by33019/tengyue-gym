package com.gym.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.dto.PlanCreateDTO;
import com.gym.dto.PlanVO;

public interface PlanService {
    PlanVO create(Long userId, PlanCreateDTO dto);
    PlanVO getById(Long userId, Long planId);
    Page<PlanVO> list(Long userId, int page, int size, Integer status);
    void update(Long userId, Long planId, PlanCreateDTO dto);
    void toggleStatus(Long userId, Long planId);
    Page<PlanVO> listTemplates(int page, int size);
}
