package com.cn.bdth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@TableName("carousel")
@Accessors(chain = true)
public class Carousel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String url;

    private LocalDateTime createdTime;

    private String navtoUrl;
}
