package com.gym.controller;

import com.gym.common.R;
import com.gym.dto.LoginDTO;
import com.gym.dto.RefreshDTO;
import com.gym.dto.RegisterDTO;
import com.gym.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final StringRedisTemplate stringRedisTemplate;

    @GetMapping("/captcha")
    public R<Object> captcha() {
        String code = String.format("%04d", (int) (Math.random() * 10000));
        String key = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        stringRedisTemplate.opsForValue().set("captcha:" + key, code, 5, TimeUnit.MINUTES);
        return R.ok(Map.of("captchaKey", key, "captchaCode", code));
    }

    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterDTO dto) {
        try {
            authService.register(dto);
            return R.ok();
        } catch (RuntimeException e) {
            log.error("注册失败: username={}", dto.getUsername(), e);
            String msg = e.getMessage() != null ? e.getMessage() : "注册失败，请稍后重试";
            return R.fail(msg);
        }
    }

    @PostMapping("/login")
    public R<Object> login(@Valid @RequestBody LoginDTO dto) {
        try {
            return R.ok(authService.login(dto));
        } catch (RuntimeException e) {
            log.error("登录失败: username={}", dto.getUsername(), e);
            String msg = e.getMessage() != null ? e.getMessage() : "登录失败，请稍后重试";
            return R.fail(msg);
        }
    }

    @PostMapping("/refresh")
    public R<Object> refresh(@Valid @RequestBody RefreshDTO dto) {
        try {
            return R.ok(authService.refresh(dto.getRefreshToken()));
        } catch (RuntimeException e) {
            log.error("Token刷新失败", e);
            String msg = e.getMessage() != null ? e.getMessage() : "Token刷新失败";
            return R.fail(msg);
        }
    }
}
