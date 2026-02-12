package com.cn.bdth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("project_star")
public class ProjectStar {

    @TableId(type = IdType.AUTO)
    private Long projectStarId;

    private Long projectId;

    private Long userId;
}
