package com.gym.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.entity.AiCallLog;
import com.gym.mapper.AiCallLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AiCallLogService {

    private final AiCallLogMapper aiCallLogMapper;

    /** 每日 AI 调用上限 */
    public static final int DAILY_LIMIT = 10;

    /** 统计当天调用次数 */
    public long countTodayCalls(Long userId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        return aiCallLogMapper.selectCount(
                new LambdaQueryWrapper<AiCallLog>()
                        .eq(AiCallLog::getUserId, userId)
                        .between(AiCallLog::getCreatedAt, start, end));
    }

    /** 检查是否超过每日上限 */
    public boolean isLimitExceeded(Long userId) {
        return countTodayCalls(userId) >= DAILY_LIMIT;
    }

    /** 剩余调用次数 */
    public long remainingCalls(Long userId) {
        long used = countTodayCalls(userId);
        return Math.max(0, DAILY_LIMIT - used);
    }

    /** 记录一次调用 */
    public void log(Long userId, String callType, String sessionId, String requestSummary, String responseSummary) {
        AiCallLog log = new AiCallLog();
        log.setUserId(userId);
        log.setCallType(callType);
        log.setSessionId(sessionId);
        log.setRequestSummary(requestSummary);
        log.setResponseSummary(responseSummary);
        aiCallLogMapper.insert(log);
    }
}
