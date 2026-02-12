package com.cn.bdth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@TableName("chat")
@Accessors(chain = true)
public class Chat {

    @TableId(type =  IdType.AUTO)
    private Long id;

    private String type;

    private String content;

    private Long userId;

    private Long chatListId;

    private LocalDateTime createdTime;
}

