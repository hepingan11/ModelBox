package com.cn.bdth.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RootDto {

    private String userName;

    private String password;

    private String adminKey;
}

