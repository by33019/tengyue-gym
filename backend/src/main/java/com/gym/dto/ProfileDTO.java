package com.gym.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProfileDTO {
    private Double height;
    private Double weight;
    private String fitnessGoal;
    private String fitnessLevel;
}
