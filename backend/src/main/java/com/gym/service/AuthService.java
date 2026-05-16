package com.gym.service;

import com.gym.dto.LoginDTO;
import com.gym.dto.LoginVO;
import com.gym.dto.RegisterDTO;
import com.gym.dto.TokenVO;

public interface AuthService {
    void register(RegisterDTO dto);
    LoginVO login(LoginDTO dto);
    TokenVO refresh(String refreshToken);
}
