package com.gym.controller;

import com.gym.common.R;
import com.gym.dto.PasswordDTO;
import com.gym.dto.ProfileDTO;
import com.gym.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public R<Object> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public R<Void> updateProfile(@RequestBody ProfileDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateProfile(userId, dto);
        return R.ok();
    }

    @PutMapping("/password")
    public R<Void> changePassword(@Valid @RequestBody PasswordDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            userService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
            return R.ok();
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }
}
