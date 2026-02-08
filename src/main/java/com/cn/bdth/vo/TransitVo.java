package com.cn.bdth.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TransitVo {

    //状态: DRAW绘画| TEXT文字
    private String type;

    //开头文本，仅type为DRAW时有效(正在为您绘制....的图片)
    private String content;

    private String size;

}
