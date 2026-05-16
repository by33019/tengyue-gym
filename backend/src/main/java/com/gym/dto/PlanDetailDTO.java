package com.gym.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlanDetailDTO {
    private Long id;
    @NotNull(message = "星期不能为空")
    private Integer dayOfWeek;
    @NotBlank(message = "运动类型不能为空")
    private String exerciseType;
    @Min(0) private Integer sets;
    @Min(0) private Integer reps;
    @Min(0) private Integer duration;
    private String note;
}
