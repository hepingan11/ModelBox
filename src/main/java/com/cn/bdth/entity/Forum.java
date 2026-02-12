package com.cn.bdth.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@TableName("forum")
@Accessors(chain = true)
public class Forum {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  private String title;

  private String content;

  private Long userId;

  private Integer look;

  private Integer likes;

  private Integer status;

  private LocalDateTime createdTime;

}
