package com.cn.bdth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("frame_unlock")
public class FrameUnlock {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long frameId;

    private LocalDateTime createdTime;
}
