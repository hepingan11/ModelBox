package com.cn.bdth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("project_field")
public class ProjectField {

    @TableId(type = IdType.AUTO)
    private Long projectFieldId;

    private String fieldName;

    private Long parentId;
}
