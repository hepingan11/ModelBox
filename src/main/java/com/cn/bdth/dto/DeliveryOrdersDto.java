package com.cn.bdth.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class DeliveryOrdersDto {
    private Long deliveryOrdersId;

    //顾客用户id
    private Long customerId;

    //顾客地址id
    private Long addressId;

    //配送人id
    private Long recipientId;

    //配送费
    private Double deliveryFee;

    //配送目标地址
    private String goalingAddress;

    //使用的优惠券id
    private Long couponId;

    //学校id
    private Long schoolId;

    //宿舍id
    private Long dormitoryId;

    //起始地址（取货地址）
    private String startingAddress;

    //创建时间
    private LocalDateTime createdTime;

    //外卖预计到达时间
    private LocalDateTime takeoutDeliveryTime;

    //外卖订单号
    private String takeoutNo;

    //送达时间
    private LocalDateTime deliveryTime;

    //配送商品价值
    private Double deliveryShopValue;

    //备注
    private String note;

    //是否匿名
    private Boolean isNick;

    //愿意等待时间（分）
    private Integer waitTime;

    //是否送上楼
    private Boolean isFloor;
}
