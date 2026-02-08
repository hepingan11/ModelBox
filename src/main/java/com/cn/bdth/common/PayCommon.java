package com.cn.bdth.common;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PayCommon {

    private Double price;

    //描述
    private String description;

    private String prepayId;

    private String notifyUrl;
}
