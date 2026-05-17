package com.gym.controller;

import com.gym.common.R;
import com.gym.dto.AiRequest;
import com.gym.service.AiService;
import com.gym.service.AiCallLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    private SseEmitter forbidden() {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event().data("{\"code\":403,\"message\":\"仅学员和教练可使用AI功能\"}"));
        } catch (IOException ignored) {}
        emitter.complete();
        return emitter;
    }

    private boolean canUseAi(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        return role != null && role <= 1;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@Valid @RequestBody AiRequest req, HttpServletRequest request) {
        if (!canUseAi(request)) return forbidden();
        Long userId = (Long) request.getAttribute("userId");
        return aiService.streamRun(userId, "chat", req.getMessage());
    }

    @PostMapping(value = "/plan", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter plan(@Valid @RequestBody AiRequest req, HttpServletRequest request) {
        if (!canUseAi(request)) return forbidden();
        Long userId = (Long) request.getAttribute("userId");
        return aiService.streamRun(userId, "plan", req.getMessage());
    }

    @PostMapping(value = "/analyze", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter analyze(@Valid @RequestBody AiRequest req, HttpServletRequest request) {
        if (!canUseAi(request)) return forbidden();
        Long userId = (Long) request.getAttribute("userId");
        return aiService.streamRun(userId, "analyze", req.getMessage());
    }

    @PostMapping(value = "/recipe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter recipe(@Valid @RequestBody AiRequest req, HttpServletRequest request) {
        if (!canUseAi(request)) return forbidden();
        Long userId = (Long) request.getAttribute("userId");
        return aiService.streamRun(userId, "recipe", req.getMessage());
    }

    @PostMapping(value = "/exercise-guide", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter exerciseGuide(@Valid @RequestBody AiRequest req, HttpServletRequest request) {
        if (!canUseAi(request)) return forbidden();
        Long userId = (Long) request.getAttribute("userId");
        return aiService.streamRun(userId, "exercise_guide", req.getMessage());
    }

    @GetMapping("/quota")
    public R<Map<String, Object>> quota(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role > 1) return R.fail("仅学员和教练可使用AI功能");
        Long userId = (Long) request.getAttribute("userId");
        long remaining = aiService.getQuota(userId);
        return R.ok(Map.of(
                "remaining", remaining,
                "dailyLimit", AiCallLogService.DAILY_LIMIT
        ));
    }
}
