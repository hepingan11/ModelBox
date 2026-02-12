package com.cn.bdth.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ChatDto {
    private Long chatListId;

    private String content;

    private String type;
}
