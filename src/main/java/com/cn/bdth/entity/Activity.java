package com.cn.bdth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@TableName("activity")
@Accessors(chain = true)
public class Activity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private String type;

    private Long  userId;

    private String url;

    private String image;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
