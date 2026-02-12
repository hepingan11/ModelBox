package com.cn.bdth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("group")
public class Group {

    @TableId(type = IdType.AUTO)
    private Integer groupId;

    private String groupName;

    private String projectId;

    private LocalDateTime lastMessageTime;

    private String lastMessage;

    private String avatar;
}
