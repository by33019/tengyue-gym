package com.gym.dto;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private String refreshToken;
    private String username;
    private Integer role;
}
