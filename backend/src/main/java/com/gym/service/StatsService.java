package com.gym.service;

import java.util.List;
import java.util.Map;

public interface StatsService {
    Map<String, Object> getSummary(Long userId);
    Map<String, Object> getTrend(Long userId, String period);
    List<Map<String, Object>> getRanking(String type, String period);
}
