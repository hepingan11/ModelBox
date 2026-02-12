package com.cn.bdth.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class ChatVo {
    private Long id;

    private String type;

    private String content;

    private Long userId;

    private Long chatListId;

    private LocalDateTime createdTime;

    //是否是当前用户的信息
    private Boolean isUser;
}
