package com.cn.bdth.common;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class WxRequestParam {

    private String timeStamp;

    private String nonceStr;

    private String packageValue;

    private String signType;

    private String paySign;
}
