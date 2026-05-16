package com.gym.controller;

import com.gym.common.R;
import com.gym.dto.PasswordDTO;
import com.gym.dto.ProfileDTO;
import com.gym.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${app.upload.dir:uploads/avatars}")
    private String uploadDir;

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

        // 校验类型与大小
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg")
                && !contentType.equals("image/png") && !contentType.equals("image/gif"))) {
            return R.fail("仅支持 jpg/png/gif 格式的图片");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return R.fail("图片大小不能超过 5MB");
        }

        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);

            String ext = getExtension(file.getOriginalFilename());
            String filename = userId + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());

            String avatarUrl = "/uploads/avatars/" + filename;
            userService.updateAvatar(userId, avatarUrl);
            return R.ok(avatarUrl);
        } catch (IOException e) {
            return R.fail("头像上传失败");
        }
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

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
