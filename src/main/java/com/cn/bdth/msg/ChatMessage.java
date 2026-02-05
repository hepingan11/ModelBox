package com.cn.bdth.msg;

import lombok.Data;
import lombok.experimental.Accessors;
import reactor.core.publisher.Flux;

@Data
@Accessors(chain = true)
public class ChatMessage {

    private Flux<String> content;

    private String media;

    private String type;
}
