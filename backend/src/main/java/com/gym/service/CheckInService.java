package com.gym.service;

import com.gym.dto.CheckInDTO;
import com.gym.dto.CheckInQuery;
import com.gym.entity.CheckIn;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Map;

public interface CheckInService {
    CheckIn create(Long userId, CheckInDTO dto);
    Page<CheckIn> list(Long userId, CheckInQuery query);
    Map<String, Integer> getCalendar(Long userId, int year, int month);
    int getTodayCount(Long userId);
    byte[] export(Long userId, String format);
}
