package com.cn.bdth.msg;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class WxPayResponse {

    private String orderId;

    private String codeUrl;
}
