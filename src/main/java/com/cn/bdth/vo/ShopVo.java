package com.cn.bdth.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShopVo {
    private Long id;

    private String shopName;

    private Double price;

    private Long userId;

    private String introduce;

    private Boolean isOnline;

    private Integer status;

    private String reason;

    private LocalDateTime createdTime;

    private List<String> imageUrl;

    private String username;

    private String category;

    private String avatar;

    private String avatarFrameUrl;

    private Integer level;

    private Boolean isFavorite;
}
