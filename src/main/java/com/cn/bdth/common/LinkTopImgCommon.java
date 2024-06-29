package com.cn.bdth.common;

import com.cn.bdth.config.LinkTopImgConfig;
import com.cn.bdth.constants.ServerConstant;
import com.cn.bdth.utils.RedisUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Data
public class LinkTopImgCommon {

    private final RedisUtils redisUtils;

    private final LinkTopImgConfig linkTopImgConfig;

    public String getLinkTopImg() {
        final Object value = redisUtils.getValue(ServerConstant.LINK_TOP_IMG);
        if (value == null) {
            return linkTopImgConfig.getLinkTopImg();
        }
        try {
            return String.valueOf(value);
        } catch (Exception e) {
            log.warn("已清除上一个版本的Link Top Img配置,请前往控制台重新配置Link Top Img参数配置");
            redisUtils.delKey(ServerConstant.LINK_TOP_IMG);
            return linkTopImgConfig.getLinkTopImg();
        }
    }
}
