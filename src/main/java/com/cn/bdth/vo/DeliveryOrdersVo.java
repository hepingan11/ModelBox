package com.cn.bdth.vo;


import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class DeliveryOrdersVo {

    private Long deliveryOrdersId;

    //顾客用户id
    private Long customerId;

    //配送人id
    private Long recipientId;

    //配送费
    private Double deliveryFee;

    //目标地址id
    private Long addressId;

    //配送目标地址
    private String addressContent;

    //外卖订单号
    private String takeoutNo;

    //目标宿舍id
    private Long dormitoryId;

    //目标宿舍名称
    private String dormitoryName;

    //起始地址（取货地址）
    private String startingAddress;

    //创建时间
    private LocalDateTime createdTime;

    //送达时间
    private LocalDateTime deliveryTime;

    //外卖预计到达时间
    private LocalDateTime takeoutDeliveryTime;

    //是否送上楼
    private Boolean isFloor;



    /** 订单状态
     * SUCCESS 支付成功（等待商家制作）
     * REFUND 转入退款
     * NOTPAY 未支付
     * CLOSED 已关闭
     * REVOKED 已撤销
     * DELIVERING 配送中
     * USERPAYING 用户支付中
     * PAYERROR 支付失败
     * DONE 已完成（商家制作完成）
     */
    private String status;

    //配送商品价值
    private Double deliveryShopValue;

    //备注
    private String note;

    //微信支付商户单号
    private String outTradeNo;

    //是否匿名
    private Boolean isNick;

    //目标配送地址
    private String goalingAddress;

    //配送人姓名
    private String recipientName;

    //配送人手机号
    private String recipientPhone;

    //顾客姓名
    private String customerName;

    //顾客手机号
    private String customerPhone;
}
