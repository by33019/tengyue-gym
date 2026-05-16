package com.gym.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostDTO {
    @NotBlank(message = "内容不能为空")
    private String content;
}
