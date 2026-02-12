package com.cn.bdth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@TableName("forum_image")
@Accessors(chain = true)
public class ForumImage {

  @TableId(type = IdType.AUTO)
  private Long id;

  private Long forumId;

  private String imageUrl;

  private String traceId;

  private Boolean isPass;
}
