package com.gym.controller;

import com.gym.common.R;
import com.gym.dto.PostDTO;
import com.gym.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public R<Object> create(@Valid @RequestBody PostDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(postService.create(userId, dto));
    }

    @GetMapping("/list")
    public R<Object> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size) {
        return R.ok(postService.list(page, size));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            postService.delete(userId, id);
            return R.ok();
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    @PostMapping("/{id}/like")
    public R<Void> toggleLike(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            postService.toggleLike(userId, id);
            return R.ok();
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }
}
