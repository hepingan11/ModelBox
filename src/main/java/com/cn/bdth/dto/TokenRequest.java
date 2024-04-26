package com.cn.bdth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenRequest {
    private String email;
    private String password;
    private Integer refresh;

    // 省略getter和setter方法以及构造函数

    // 注意：这里为了方便，没有进行参数验证，请根据实际需要进行参数验证和处理
}