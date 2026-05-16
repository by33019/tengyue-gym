package com.gym.dto;

import lombok.Data;

@Data
public class CheckInQuery {
    private Integer page = 1;
    private Integer size = 10;
    private String exerciseType;
    private String startDate;
    private String endDate;
}
