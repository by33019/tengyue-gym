package com.gym.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiRequest {
    @NotBlank(message = "消息不能为空")
    private String message;
}
