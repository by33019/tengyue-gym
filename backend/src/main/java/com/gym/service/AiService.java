package com.gym.service;

import com.gym.coze.*;
import com.gym.entity.User;
import com.gym.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final CozeClient cozeClient;
    private final ContextBuilder contextBuilder;
    private final AiCallLogService aiCallLogService;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    /** 10 分钟缓存 */
    private final Map<String, CacheEntry> responseCache = new ConcurrentHashMap<>();

    private static final Map<String, String> FALLBACK_TEXTS = Map.of(
            "plan", "根据你的身体数据，建议每周进行3-4次训练，每次45-60分钟。初期以低强度有氧配合基础力量训练为主，逐步提升强度。请完善个人信息后获取更精准的计划。",
            "analyze", "从近期运动数据来看，建议保持规律的运动频率，适当增加力量训练的比例，同时注意运动前后的拉伸和营养补充。",
            "recipe", "健身期间建议：早餐以蛋白质+碳水为主（鸡蛋+燕麦），午餐均衡搭配（鸡胸肉+糙米+蔬菜），晚餐清淡（鱼+沙拉）。运动后30分钟内补充蛋白质效果最佳。",
            "exercise_guide", "进行力量训练时请注意：1）保持正确姿势比重量更重要；2）控制动作节奏，离心阶段2-3秒；3）组间休息60-90秒；4）每次训练前充分热身10分钟。",
            "recovery", "运动后恢复建议：1）训练后24-48小时是肌肉修复关键期；2）保证7-8小时充足睡眠；3）轻度拉伸有助于缓解延迟性肌肉酸痛；4）如持续疼痛超过3天请就医。",
            "chat", "我是你的专属健身助手，可以帮你制定健身计划、分析运动数据、推荐饮食方案、指导训练动作、提供伤病恢复建议。请告诉我你需要什么帮助？"
    );

    public SseEmitter streamRun(Long userId, String callType, String message) {
        SseEmitter emitter = new SseEmitter(30_000L);
        CozeSseEmitterImpl cozeEmitter = new CozeSseEmitterImpl(emitter);

        // 检查用户身体数据
        User user = userMapper.selectById(userId);
        if (user == null || user.getFitnessGoal() == null || user.getFitnessLevel() == null) {
            cozeEmitter.send("请先在个人中心完善身体数据（身高、体重、健身目标、运动等级）后再使用 AI 功能。");
            cozeEmitter.complete();
            return emitter;
        }

        // 限额检查
        if (aiCallLogService.isLimitExceeded(userId)) {
            cozeEmitter.send("今日AI调用次数已用完（每日10次），请明天再来。");
            cozeEmitter.complete();
            return emitter;
        }

        // 缓存检查
        String cacheKey = cacheKey(userId, callType, message);
        CacheEntry cached = responseCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            cozeEmitter.send(cached.content);
            cozeEmitter.complete();
            return emitter;
        }

        // 获取或创建 session
        String sessionId = getOrCreateSession(userId, callType);

        // 构建上下文
        String context = contextBuilder.buildContext(userId);
        String fullPrompt = context + "\n【用户问题】\n" + message;

        // 异步调用 Coze
        new Thread(() -> {
            StringBuilder fullResponse = new StringBuilder();
            CozeSseEmitter captureEmitter = new CozeSseEmitter() {
                @Override
                public void send(String data) {
                    fullResponse.append(data);
                    cozeEmitter.send(data);
                }
                @Override
                public void complete() {
                    if (!cozeEmitter.isCompleted()) {
                        cozeEmitter.complete();
                    }
                }
                @Override
                public void error(Throwable t) {
                    String fallback = FALLBACK_TEXTS.getOrDefault(callType, FALLBACK_TEXTS.get("chat"));
                    if (fullResponse.isEmpty()) {
                        cozeEmitter.send(fallback);
                    }
                    cozeEmitter.complete();
                    fullResponse.append(fallback);
                }
                @Override
                public boolean isCompleted() {
                    return cozeEmitter.isCompleted();
                }
            };

            try {
                cozeClient.streamRun(fullPrompt, sessionId, captureEmitter);
                // 缓存结果
                if (!fullResponse.isEmpty()) {
                    responseCache.put(cacheKey, new CacheEntry(fullResponse.toString()));
                    cleanExpiredCache();
                }
                // 记录调用日志
                aiCallLogService.log(userId, callType, sessionId,
                        message.length() > 200 ? message.substring(0, 200) : message,
                        fullResponse.toString().length() > 500 ? fullResponse.substring(0, 500) : fullResponse.toString());
            } catch (Exception e) {
                log.error("AI 调用异常", e);
                captureEmitter.error(e);
            }
        }).start();

        return emitter;
    }

    public long getQuota(Long userId) {
        return aiCallLogService.remainingCalls(userId);
    }

    private String getOrCreateSession(Long userId, String callType) {
        String key = "coze:session:" + userId + ":" + callType;
        String sessionId = stringRedisTemplate.opsForValue().get(key);
        if (sessionId == null) {
            sessionId = java.util.UUID.randomUUID().toString();
            stringRedisTemplate.opsForValue().set(key, sessionId, Duration.ofHours(24));
        }
        return sessionId;
    }

    private String cacheKey(Long userId, String callType, String message) {
        String raw = userId + ":" + callType + ":" + message;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return raw;
        }
    }

    private void cleanExpiredCache() {
        responseCache.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    static class CacheEntry {
        final String content;
        final long createdAt;

        CacheEntry(String content) {
            this.content = content;
            this.createdAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > Duration.ofMinutes(10).toMillis();
        }
    }
}
