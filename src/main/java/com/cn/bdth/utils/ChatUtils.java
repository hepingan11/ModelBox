package com.cn.bdth.utils;


import com.cn.bdth.constants.OperateConstant;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


/**
 * BOT工具类
 *
 * @author  @github dulaiduwang003
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
@Data
public class ChatUtils {

    private final RedisUtils redisUtils;

    @Async
    public void lastOperationTime(final Long userId) {
        //设置当前最后操作时间
        redisUtils.setValueTimeout(OperateConstant.USER_CALL_TIME + userId, LocalDateTime.now(), 604800);
    }
}
