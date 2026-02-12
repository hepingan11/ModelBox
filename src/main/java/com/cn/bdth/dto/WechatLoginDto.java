package com.cn.bdth.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class WechatLoginDto {

    private String code;

    private String phone;

    private String nickName;
}
