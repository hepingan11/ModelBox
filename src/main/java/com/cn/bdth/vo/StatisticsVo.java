package com.cn.bdth.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StatisticsVo {

    private Long userCount;

    private Long projectCount;

    private Long forumCount;

    private Long chatCount;
}
