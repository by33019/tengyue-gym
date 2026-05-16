package com.gym.service.impl;

import com.gym.dto.ProfileDTO;
import com.gym.dto.UserVO;
import com.gym.entity.User;
import com.gym.mapper.UserMapper;
import com.gym.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return toVO(user);
    }

    @Override
    public void updateProfile(Long userId, ProfileDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (dto.getHeight() != null) user.setHeight(java.math.BigDecimal.valueOf(dto.getHeight()));
        if (dto.getWeight() != null) user.setWeight(java.math.BigDecimal.valueOf(dto.getWeight()));
        if (dto.getFitnessGoal() != null) user.setFitnessGoal(dto.getFitnessGoal());
        if (dto.getFitnessLevel() != null) user.setFitnessLevel(dto.getFitnessLevel());
        userMapper.updateById(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setAvatar(user.getAvatar());
        vo.setHeight(user.getHeight() != null ? user.getHeight().doubleValue() : null);
        vo.setWeight(user.getWeight() != null ? user.getWeight().doubleValue() : null);
        vo.setFitnessGoal(user.getFitnessGoal());
        vo.setFitnessLevel(user.getFitnessLevel());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        return vo;
    }
}
