package com.cn.bdth.service;

import com.cn.bdth.dto.DigitalVideoDto;
import com.cn.bdth.vo.DigitalVo;

import java.util.List;

public interface DigitalService {

    /**
     * 查询模型列表
     * @return
     */
    List<DigitalVo> getVideoList();

    /**
     * 新建模型
     * @param dto
     */
    void addModel(DigitalVideoDto dto);

    void deleteModel(long id);
}
