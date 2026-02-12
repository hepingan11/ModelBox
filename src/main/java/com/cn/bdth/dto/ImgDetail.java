package com.cn.bdth.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ImgDetail {

    private String strategy;

    private Integer errcode;

    private String suggest;

    private Integer label;

    private Integer prob;
}
