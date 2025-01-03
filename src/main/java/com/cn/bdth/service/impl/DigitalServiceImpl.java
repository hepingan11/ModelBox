package com.cn.bdth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.bdth.dto.DigitalChatDto;
import com.cn.bdth.dto.DigitalVideoDto;
import com.cn.bdth.entity.Dialogue;
import com.cn.bdth.entity.Digital;
import com.cn.bdth.mapper.DigitalMapper;
import com.cn.bdth.service.DigitalService;
import com.cn.bdth.utils.UserUtils;
import com.cn.bdth.vo.DigitalChatVo;
import com.cn.bdth.vo.DigitalVo;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DigitalServiceImpl implements DigitalService {

    /**
     * 前端传给后端的叫DTO,后端传给前端的叫VO，后端中与数据库连接的叫PO(一般就是普通类)
     */
    private final DigitalMapper digitalMapper;

    /**
     * 获取视频列表
     * @return DigitalVO
     */
    @Override
    public List<DigitalVo> getVideoList() {
        return digitalMapper.selectList(
                new QueryWrapper<Digital>()
                        .lambda()
                        .select(Digital::getVideoName,Digital::getVideoUrl, Digital::getImgUrl)
        ).stream().map(c -> new DigitalVo().setVideoName(c.getVideoName()).setVideoUrl(c.getVideoUrl()).setImgUrl(c.getImgUrl())).collect(Collectors.toList());
    }

    /**
     * 新增模型
     * @param dto
     */
    @Override
    public void addModel(DigitalVideoDto dto) {
        final Digital digital =new Digital()
                .setUserID(UserUtils.getCurrentLoginId())
                .setVideoUrl(dto.getVideoUrl())
                .setVideoName(dto.getVideoName())
                .setImgUrl(dto.getImgUrl())
                .setCreatedTime(LocalDateTime.now());
        digitalMapper.insert(digital);

    }

    /**
     * 删除模型
     * @param id
     */
    @Override
    public void deleteModel(Long id) {
        digitalMapper.deleteById(id);
    }

    /**
     * 修改模型
     * @param dto
     */
    @Override
    public void updateModel(DigitalVideoDto dto) {
        digitalMapper.updateById(new Digital().setVideoUrl(dto.getVideoUrl()).setVideoName(dto.getVideoName()).setImgUrl(dto.getImgUrl()));
    }

    /**
     * 模型对话
     * @param dto
     * @return vo
     */
    @Override
    public DigitalChatVo chatModel(DigitalChatDto dto) {
        DigitalChatVo digitalChatVo =new DigitalChatVo();
        return new DigitalChatVo();
    }


}
