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

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@Valid @RequestBody AiRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return aiService.streamRun(userId, "chat", req.getMessage());
    }

    @PostMapping(value = "/plan", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter plan(@Valid @RequestBody AiRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return aiService.streamRun(userId, "plan", req.getMessage());
    }

    @PostMapping(value = "/analyze", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter analyze(@Valid @RequestBody AiRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return aiService.streamRun(userId, "analyze", req.getMessage());
    }

    @PostMapping(value = "/recipe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter recipe(@Valid @RequestBody AiRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return aiService.streamRun(userId, "recipe", req.getMessage());
    }

    @PostMapping(value = "/exercise-guide", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter exerciseGuide(@Valid @RequestBody AiRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return aiService.streamRun(userId, "exercise_guide", req.getMessage());
    }

    @GetMapping("/quota")
    public R<Map<String, Object>> quota(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        long remaining = aiService.getQuota(userId);
        return R.ok(Map.of(
                "remaining", remaining,
                "dailyLimit", AiCallLogService.DAILY_LIMIT
        ));
    }
}
