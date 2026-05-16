package com.gym.controller;

import com.gym.common.R;
import com.gym.service.StatsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/summary")
    public R<Object> summary(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(statsService.getSummary(userId));
    }

    @GetMapping("/trend")
    public R<Object> trend(@RequestParam(defaultValue = "week") String period, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(statsService.getTrend(userId, period));
    }

    @GetMapping("/ranking")
    public R<Object> ranking(@RequestParam(defaultValue = "days") String type,
                              @RequestParam(defaultValue = "week") String period) {
        return R.ok(statsService.getRanking(type, period));
    }
}
