package com.gym.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("`ai_call_log`")
public class AiCallLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String callType;
    private String sessionId;
    private String requestSummary;
    private String responseSummary;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
}
