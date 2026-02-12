package com.cn.bdth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("project_member")
public class ProjectMember {

    private Long projectMemberId;

    private Long projectId;

    private Long userId;

    private String position;

    private Boolean isNick;

    private String status;

    private LocalDateTime createdTime;
}
