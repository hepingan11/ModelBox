package com.cn.bdth.vo.admin;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DrawVo {

    private Long id;

    private String image;
}
