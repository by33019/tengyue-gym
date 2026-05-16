package com.gym.service;

import com.gym.dto.LoginDTO;
import com.gym.dto.LoginVO;
import com.gym.dto.ProfileDTO;
import com.gym.dto.RegisterDTO;
import com.gym.mapper.UserMapper;
import com.gym.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    private Long userId;

    @BeforeEach
    void setUp() {
        String username = "p_" + System.nanoTime();
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(username);
        dto.setPassword("pass123");
        authService.register(dto);

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        userId = user.getId();
    }

    @Test
    void shouldGetProfile() {
        var profile = userService.getProfile(userId);
        assertNotNull(profile);
        assertNotNull(profile.getUsername());
    }

    @Test
    void shouldUpdateProfile() {
        ProfileDTO dto = new ProfileDTO();
        dto.setHeight(175.0);
        dto.setWeight(70.0);
        dto.setFitnessGoal("增肌");
        dto.setFitnessLevel("进阶");

        userService.updateProfile(userId, dto);

        var profile = userService.getProfile(userId);
        assertEquals(175.0, profile.getHeight());
        assertEquals(70.0, profile.getWeight());
        assertEquals("增肌", profile.getFitnessGoal());
    }

    @Test
    void shouldChangePassword() {
        userService.changePassword(userId, "pass123", "newpass456");
    }

    @Test
    void shouldRejectWrongOldPassword() {
        assertThrows(RuntimeException.class, () ->
                userService.changePassword(userId, "wrongold", "newpass"));
    }
}
