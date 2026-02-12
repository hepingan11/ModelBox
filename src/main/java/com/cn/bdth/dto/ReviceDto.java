package com.cn.bdth.dto;


import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ReviceDto {

    private String ToUserName;

    private String FromUserName;

    private String CreateTime;

    private String MsgType;

    private String Event;

    private String debug_str;

    private String trace_id;

    private ImgResult result;

    private ImgDetail detail;

}

