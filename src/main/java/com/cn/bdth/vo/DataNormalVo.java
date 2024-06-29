package com.cn.bdth.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Data
@Accessors(chain = true)
public class DataNormalVo {
    private String  img;

    private String top1;

    private String top2;

    private String top3;

    private String  value1;

    private String  value2;

    private String value3;

}
