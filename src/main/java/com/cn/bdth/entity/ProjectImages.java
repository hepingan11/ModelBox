package com.cn.bdth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("project_images")
public class ProjectImages {

    @TableId(type = IdType.AUTO)
    private Long projectImagesId;

    private Long projectId;

    private String imageUrl;
}
