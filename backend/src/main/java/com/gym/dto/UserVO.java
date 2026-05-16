package com.gym.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String avatar;
    private Double height;
    private Double weight;
    private String fitnessGoal;
    private String fitnessLevel;
    private Integer role;
    private Integer status;
    private Integer isAnonymous;
}
