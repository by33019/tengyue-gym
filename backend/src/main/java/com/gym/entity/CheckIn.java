package com.gym.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("`check_in`")
public class CheckIn {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String exerciseType;
    private Integer durationMinutes;
    private Integer calories;
    private String imageUrl;
    private String note;
    private LocalDateTime checkInTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
}
