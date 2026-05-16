package com.gym.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("`plan`")
public class Plan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String planName;
    private String goal;
    private String difficulty;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private String source;
    private Integer isTemplate;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
}
