package com.gym.controller;

import com.gym.common.R;
import com.gym.dto.PlanCreateDTO;
import com.gym.service.PlanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plan")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping
    public R<Object> create(@Valid @RequestBody PlanCreateDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            return R.ok(planService.create(userId, dto));
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    @GetMapping("/list")
    public R<Object> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) Integer status,
                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(planService.list(userId, page, size, status));
    }

    @GetMapping("/{id}")
    public R<Object> getDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            return R.ok(planService.getById(userId, id));
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody PlanCreateDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            planService.update(userId, id, dto);
            return R.ok();
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public R<Void> toggleStatus(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            planService.toggleStatus(userId, id);
            return R.ok();
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    @GetMapping("/templates")
    public R<Object> templates(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size) {
        return R.ok(planService.listTemplates(page, size));
    }

    @PutMapping("/{id}/publish")
    public R<Void> publishTemplate(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try { planService.publishTemplate(userId, id); return R.ok(); }
        catch (RuntimeException e) { return R.fail(e.getMessage()); }
    }

    @PutMapping("/{id}/unpublish")
    public R<Void> unpublishTemplate(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try { planService.unpublishTemplate(userId, id); return R.ok(); }
        catch (RuntimeException e) { return R.fail(e.getMessage()); }
    }

    @PostMapping("/{id}/apply")
    public R<Object> applyTemplate(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try { return R.ok(planService.applyTemplate(userId, id)); }
        catch (RuntimeException e) { return R.fail(e.getMessage()); }
    }
}
