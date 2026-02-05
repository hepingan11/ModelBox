package com.cn.bdth.constants;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class DrawKeyWordsConstant {

    public static final Set<String> PAINT_KEYWORDS = new HashSet<>();

    static {
        // 基础绘画动作
        PAINT_KEYWORDS.add("绘画");
        PAINT_KEYWORDS.add("绘制");
        PAINT_KEYWORDS.add("画一张");
        PAINT_KEYWORDS.add("画一幅");
        // AI绘画相关
        PAINT_KEYWORDS.add("ai画");
        PAINT_KEYWORDS.add("ai绘画");
        PAINT_KEYWORDS.add("生成图片");
        PAINT_KEYWORDS.add("生成画作");
        PAINT_KEYWORDS.add("文生图");
        // 设计/制图相关（含绘画属性）
        PAINT_KEYWORDS.add("设计");
        PAINT_KEYWORDS.add("手绘");
        PAINT_KEYWORDS.add("素描");
        PAINT_KEYWORDS.add("彩绘");
        PAINT_KEYWORDS.add("插画");
        PAINT_KEYWORDS.add("漫画");
        PAINT_KEYWORDS.add("卡通画");
        PAINT_KEYWORDS.add("油画");
        PAINT_KEYWORDS.add("水彩画");
        // 辅助绘画需求
        PAINT_KEYWORDS.add("帮我画");
        PAINT_KEYWORDS.add("想画");
        PAINT_KEYWORDS.add("能不能画");
        PAINT_KEYWORDS.add("需要一幅画");
    }
}
