package com.cn.bdth.service;

import com.cn.bdth.dto.DrawingSdTaskDto;
import com.cn.bdth.dto.ZhipuDrawDto;
import com.cn.bdth.entity.Drawing;
import com.cn.bdth.vo.DrawingTaskVo;
import com.cn.bdth.vo.admin.DrawingVo;

import java.util.List;

public interface DrawService {


    String addZhipuDrawingTask(ZhipuDrawDto dto);

}
