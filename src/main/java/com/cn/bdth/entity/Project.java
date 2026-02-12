package com.cn.bdth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("project")
public class Project {

    @TableId(type = IdType.AUTO)
    private Long projectId;

    private String projectName;

    private String introduce;

    private Long userId;

    private LocalDateTime createdTime;

    private String status;

    private String failReason;

    private String city;

    private String ageRequirement;

    private Long projectFieldId;
}
