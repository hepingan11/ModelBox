package com.cn.bdth.service;

import com.cn.bdth.dto.DrawingSdTaskDto;
import com.cn.bdth.dto.ZhipuDrawDto;
import com.cn.bdth.entity.Drawing;
import com.cn.bdth.vo.DrawingTaskVo;
import com.cn.bdth.vo.admin.DrawingVo;

import java.util.List;

public interface DrawService {
    String addSdTask(DrawingSdTaskDto dto);

    List<String> getSdDrawList();

    List<DrawingVo> getSdDrawListPrivate();

    void setPublic(String url);

    void deleteDraw(Long id);

    String checkSdConnectivity();

    String addZhipuDrawingTask(ZhipuDrawDto dto);

}
