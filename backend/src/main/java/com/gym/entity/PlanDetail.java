package com.gym.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("`plan_detail`")
public class PlanDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private Integer dayOfWeek;
    private String exerciseType;
    private Integer sets;
    private Integer reps;
    private Integer duration;
    private String note;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
}
