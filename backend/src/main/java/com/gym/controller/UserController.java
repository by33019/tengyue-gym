package com.gym.controller;

import com.gym.common.R;
import com.gym.dto.PasswordDTO;
import com.gym.dto.ProfileDTO;
import com.gym.service.FileService;
import com.gym.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileService fileService;

    @GetMapping("/profile")
    public R<Object> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return R.ok(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public R<Void> updateProfile(@RequestBody ProfileDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            userService.updateProfile(userId, dto);
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
        return R.ok();
    }

    @PostMapping("/avatar")
    public R<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg")
                && !contentType.equals("image/png") && !contentType.equals("image/gif"))) {
            return R.fail("仅支持 jpg/png/gif 格式的图片");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return R.fail("图片大小不能超过 5MB");
        }

        try {
            String url = fileService.upload(file);
            userService.updateAvatar(userId, url);
            return R.ok(url);
        } catch (RuntimeException e) {
            return R.fail("头像上传失败: " + e.getMessage());
        }
    }

    @PutMapping("/anonymous")
    public R<Void> toggleAnonymous(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.toggleAnonymous(userId);
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
