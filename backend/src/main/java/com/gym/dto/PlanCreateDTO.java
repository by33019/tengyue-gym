package com.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class PlanCreateDTO {
    @NotBlank(message = "计划名称不能为空")
    private String planName;
    @NotBlank(message = "健身目标不能为空")
    private String goal;
    @NotBlank(message = "难度不能为空")
    private String difficulty;
    @NotNull(message = "开始日期不能为空")
    private String startDate;
    @NotNull(message = "结束日期不能为空")
    private String endDate;
    private String source = "手动";
    private List<PlanDetailDTO> details;
}
