package com.cn.bdth.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors (chain = true)
public class FrameLockDto {

    private Long id;

    private Long userId;
}
