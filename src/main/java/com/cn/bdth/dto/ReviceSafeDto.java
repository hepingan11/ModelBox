package com.cn.bdth.dto;

import lombok.Data;
import lombok.experimental.Accessors;


//微信消息推送接收类
@Data
@Accessors(chain = true)
public class ReviceSafeDto {

    private String ToUserName;

    private String  Encrypt;
}
