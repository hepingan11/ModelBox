package com.cn.bdth.service;

import com.cn.bdth.dto.DrawingSdTaskDto;
import com.cn.bdth.vo.DrawingTaskVo;

import java.util.List;

public interface DrawService {
    String addSdTask(DrawingSdTaskDto dto);

    List<String> getSdDrawList();

    List<String> getSdDrawListPrivate();
}
