package com.gym.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckInDTO {
    @NotBlank(message = "运动类型不能为空")
    private String exerciseType;

    @NotNull(message = "运动时长不能为空")
    @Min(value = 1, message = "运动时长至少1分钟")
    private Integer durationMinutes;

    private Integer calories;
    private String note;
}
