package com.cn.bdth.common;

import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PayReturnInfoCommon {

    private PrepayWithRequestPaymentResponse prepayWithRequestPaymentResponse;

    private String outTradeNo;
}
