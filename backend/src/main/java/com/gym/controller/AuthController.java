package com.gym.controller;

import com.gym.common.R;
import com.gym.dto.LoginDTO;
import com.gym.dto.RefreshDTO;
import com.gym.dto.RegisterDTO;
import com.gym.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterDTO dto) {
        try {
            authService.register(dto);
            return R.ok();
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    @PostMapping("/login")
    public R<Object> login(@Valid @RequestBody LoginDTO dto) {
        try {
            return R.ok(authService.login(dto));
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public R<Object> refresh(@Valid @RequestBody RefreshDTO dto) {
        try {
            return R.ok(authService.refresh(dto.getRefreshToken()));
        } catch (RuntimeException e) {
            return R.fail(e.getMessage());
        }
    }
}
