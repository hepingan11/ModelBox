package com.cn.bdth.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderDto {
    private Integer userId;

    /** 订单状态
     * SUCCESS 支付成功（等待商家制作）
     * REFUND 转入退款
     * NOTPAY 未支付
     * CLOSED 已关闭
     * DONE 已完成（商家制作完成）
     */

}
