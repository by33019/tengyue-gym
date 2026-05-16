package com.gym.controller;

import com.gym.common.R;
import com.gym.dto.CheckInDTO;
import com.gym.dto.CheckInQuery;
import com.gym.service.CheckInService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    public R<Object> create(@Valid @RequestBody CheckInDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            return R.ok(checkInService.create(userId, dto));
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    @GetMapping("/list")
    public R<Object> list(CheckInQuery query, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(checkInService.list(userId, query));
    }

    @GetMapping("/calendar")
    public R<Object> calendar(@RequestParam int year, @RequestParam int month, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(checkInService.getCalendar(userId, year, month));
    }

    @GetMapping("/today-count")
    public R<Integer> todayCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(checkInService.getTodayCount(userId));
    }

    @GetMapping("/export")
    public R<String> exportRecords(@RequestParam(defaultValue = "csv") String format, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        byte[] data = checkInService.export(userId, format);
        return R.ok(new String(data, java.nio.charset.StandardCharsets.UTF_8));
    }
}
