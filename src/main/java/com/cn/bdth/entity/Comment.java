package com.cn.bdth.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
  @TableId(type = IdType.AUTO)
  private Long id;

  private Long forumId;

  private Long userId;

  private String content;

  private Integer likes;

  private Integer isParent;

  private Long parentId;

  private String img;

  private LocalDateTime createdTime;

}
