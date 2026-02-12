package com.cn.bdth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@TableName("chat_list")
@Accessors (chain = true)
public class ChatList {

    @TableId(type =  IdType.AUTO)
    private Integer id;

    // 用户1的id
    private Long user1Id;

    // 用户2的id
    private Long user2Id;

    // 用户1的未读消息
    private Integer user1Message;

    // 用户2的未读消息
    private Integer user2Message;

    private LocalDateTime createdTime;

    // 最后一条消息
    private String lastMessage;
    // 最后一条消息的时间
    private LocalDateTime lastTime;
}
