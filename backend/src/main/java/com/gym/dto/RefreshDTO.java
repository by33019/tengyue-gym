package com.gym.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshDTO {
    @NotBlank(message = "Refresh Token 不能为空")
    private String refreshToken;
}
