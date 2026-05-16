package com.gym.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("`post`")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String content;
    private String imageUrls;
    private Integer likeCount;
    private Integer commentCount;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
}
