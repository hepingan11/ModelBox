package com.cn.bdth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("report")
public class Report {

    @TableId(type = IdType.AUTO)
    private Long id;
    //举报类型
    private String type;
    //被举报的内容的id
    private Long typeId;

    private Long userId;

    private String content;
    // 被举报人id
    private Long reportUserId;
    // 0：处理中 1：处理完成
    private Integer status;

    private String image;

    private LocalDateTime createdTime;
}
