package com.cn.bdth.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class ChatListVo {

    private Integer id;
    //  当前人
    private Long initiateUserId;
    //  接收人
    private Long targetUserId;
    // 当前人未读消息
    private Integer initiateMessage;
    // 创建时间
    private LocalDateTime createdTime;
    // 最后一条消息
    private String lastMessage;
    // 最后一条消息的时间
    private LocalDateTime lastTime;
    //  当前目标人名称
    private String targetUserName;
    //  当前目标人头像
    private String targetUserAvatar;
    //  当前目标人id
    private Long targetId;


}
