package com.gym.controller;

import com.gym.common.R;
import com.gym.dto.CommentDTO;
import com.gym.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public R<Object> create(@Valid @RequestBody CommentDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(commentService.create(userId, dto));
    }

    @GetMapping("/list")
    public R<Object> list(@RequestParam Long postId) {
        return R.ok(commentService.listByPost(postId));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            commentService.delete(userId, id);
            return R.ok();
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }
}
