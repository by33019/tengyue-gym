package com.gym.dto;

import lombok.Data;

@Data
public class LoginVO {
    private Long id;
    private String token;
    private String refreshToken;
    private String username;
    private Integer role;
    private String avatar;
}
