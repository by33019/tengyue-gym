package com.gym.service.impl;

import com.gym.dto.LoginDTO;
import com.gym.dto.LoginVO;
import com.gym.dto.RegisterDTO;
import com.gym.dto.TokenVO;
import com.gym.entity.User;
import com.gym.mapper.UserMapper;
import com.gym.security.JwtUtils;
import com.gym.service.AuthService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void register(RegisterDTO dto) {
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (existing != null) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        int role = dto.getRole() != null ? dto.getRole() : 0;
        if (role != 0 && role != 1) throw new RuntimeException("角色仅限普通用户或教练");
        user.setRole(role);
        user.setStatus(1);
        user.setFitnessGoal(dto.getFitnessGoal());
        user.setFitnessLevel(dto.getFitnessLevel());
        userMapper.insert(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // 验证码校验
        if (StringUtils.hasText(dto.getCaptchaKey()) || StringUtils.hasText(dto.getCaptchaCode())) {
            if (!StringUtils.hasText(dto.getCaptchaKey()) || !StringUtils.hasText(dto.getCaptchaCode())) {
                throw new RuntimeException("请输入验证码");
            }
            String cachedCode = stringRedisTemplate.opsForValue().get("captcha:" + dto.getCaptchaKey());
            if (cachedCode == null) {
                throw new RuntimeException("验证码已过期，请刷新");
            }
            if (!cachedCode.equalsIgnoreCase(dto.getCaptchaCode())) {
                throw new RuntimeException("验证码错误");
            }
            stringRedisTemplate.delete("captcha:" + dto.getCaptchaKey());
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        LoginVO vo = new LoginVO();
        vo.setId(user.getId());
        vo.setToken(jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole()));
        vo.setRefreshToken(jwtUtils.generateRefreshToken(user.getId()));
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        vo.setAvatar(user.getAvatar());
        return vo;
    }

    @Override
    public TokenVO refresh(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh Token 无效或已过期");
        }
        Long userId = Long.valueOf(jwtUtils.parseToken(refreshToken).getSubject());
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        TokenVO vo = new TokenVO();
        vo.setToken(jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole()));
        return vo;
    }
}
