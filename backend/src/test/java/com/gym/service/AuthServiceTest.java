package com.gym.service;

import com.gym.dto.LoginDTO;
import com.gym.dto.LoginVO;
import com.gym.dto.RegisterDTO;
import com.gym.dto.TokenVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void shouldRegisterNewUser() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("password123");
        dto.setFitnessGoal("增肌");
        dto.setFitnessLevel("进阶");

        authService.register(dto);

        // 验证用户已创建
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("newuser");
        loginDTO.setPassword("password123");
        LoginVO result = authService.login(loginDTO);
        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals("newuser", result.getUsername());
    }

    @Test
    void shouldRejectDuplicateUsername() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("duplicate");
        dto.setPassword("password123");

        authService.register(dto);

        assertThrows(RuntimeException.class, () -> authService.register(dto));
    }

    @Test
    void shouldLoginWithCorrectPassword() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("loginuser");
        dto.setPassword("correct123");
        authService.register(dto);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("loginuser");
        loginDTO.setPassword("correct123");
        LoginVO result = authService.login(loginDTO);

        assertNotNull(result.getToken());
        assertTrue(result.getToken().length() > 0);
    }

    @Test
    void shouldRejectWrongPassword() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("pwduser");
        dto.setPassword("right");
        authService.register(dto);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("pwduser");
        loginDTO.setPassword("wrong");

        assertThrows(RuntimeException.class, () -> authService.login(loginDTO));
    }

    @Test
    void shouldRefreshToken() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("refreshuser");
        dto.setPassword("pass123");
        authService.register(dto);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("refreshuser");
        loginDTO.setPassword("pass123");
        LoginVO loginResult = authService.login(loginDTO);

        TokenVO refreshResult = authService.refresh(loginResult.getRefreshToken());
        assertNotNull(refreshResult.getToken());
    }
}
