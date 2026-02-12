package com.cn.bdth.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class CouponUserVo {

    private Integer userId;

    private String userName;

    private String avatar;

    private String status;

    private Integer type;

    private Double discount;

    //领取时间
    private LocalDateTime gettingTime;

    //使用时间
    private LocalDateTime usedTime;

    //失效时间
    private LocalDateTime expireTime;
}
