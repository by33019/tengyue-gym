package com.gym.service;

import com.gym.dto.ProfileDTO;
import com.gym.dto.UserVO;

public interface UserService {
    UserVO getProfile(Long userId);
    void updateProfile(Long userId, ProfileDTO dto);
    void updateAvatar(Long userId, String avatarUrl);
    void toggleAnonymous(Long userId);
    void changePassword(Long userId, String oldPassword, String newPassword);
}
