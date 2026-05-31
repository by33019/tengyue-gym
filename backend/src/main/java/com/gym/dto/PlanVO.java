package com.gym.dto;

import lombok.Data;
import java.util.List;

@Data
public class PlanVO {
    private Long id;
    private Long userId;
    private String planName;
    private String goal;
    private String difficulty;
    private String startDate;
    private String endDate;
    private Integer status;
    private String source;
    private List<PlanDetailDTO> details;
    private Integer executionRate;
    private Boolean needAdjust;
}
