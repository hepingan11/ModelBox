package com.cn.bdth.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.bdth.common.StableDiffusionCommon;
import com.cn.bdth.constants.ServerConstant;
import com.cn.bdth.dto.DrawingSdTaskDto;
import com.cn.bdth.entity.Drawing;
import com.cn.bdth.entity.User;
import com.cn.bdth.enums.FileEnum;
import com.cn.bdth.exceptions.FrequencyException;
import com.cn.bdth.mapper.DrawingMapper;
import com.cn.bdth.mapper.UserMapper;
import com.cn.bdth.model.SdDrawingModel;
import com.cn.bdth.service.DrawService;
import com.cn.bdth.utils.AliUploadUtils;
import com.cn.bdth.utils.RedisUtils;
import com.cn.bdth.utils.UserUtils;
import com.cn.bdth.vo.admin.DrawingVo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DrawServiceImpl implements DrawService {

    private final DrawingMapper drawingMapper;

    private final UserMapper userMapper;

    private final StableDiffusionCommon stableDiffusionCommon;

    private final AliUploadUtils aliUploadUtils;

    private final RedisUtils redisUtils;


    /**
     * 发送绘画请求
     * @param dto
     * @return
     */
    @Override
    public String addSdTask(final DrawingSdTaskDto dto) {
        //获取当前登录用户的ID
        final Long currentLoginId = UserUtils.getCurrentLoginId();
        if (userMapper.getFrequencyById(currentLoginId) <= 7L) {
            throw new FrequencyException();
        }


//        final SdDrawingModel model = DrawingSdTaskDto.convertToPictureImgModel(dto);
        // 设置请求的URL地址
        CloseableHttpClient aDefault = HttpClients.createDefault();

        final StableDiffusionCommon.StableDiffusionStructure sdStructure = (StableDiffusionCommon.StableDiffusionStructure) redisUtils.getValue(ServerConstant.SD_CONFIG);

        String sdUrl = sdStructure.getSdUrl();
        HttpPost httpPost =new HttpPost(sdUrl+"/sdapi/v1/txt2img");
        JSONObject param = new JSONObject();
        param.put("prompt", dto.getPrompt());
        param.put("step",dto.getSteps());
        param.put("height",dto.getHeight());
        param.put("width",dto.getWidth());
        param.put("sampler_name",dto.getSampler_index());
        JSONObject seed = new JSONObject();
        seed.put("sd_model_checkpoint",dto.getModelName());
        param.put("override_settings",seed);
        if (dto.getNegative_prompt()!=null){
            param.put("negative_prompt",dto.getNegative_prompt());
        }
        if (dto.getImages()!=null){
            String url= dto.getImages().toString();
            param.put("init_images",url);
            httpPost.setURI(URI.create(sdUrl+"/sdapi/v1/img2img"));
        }

        StringEntity stringEntity= null;
        try {
            stringEntity = new StringEntity(param.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        //TODO 设置请求头
        Header header=new BasicHeader("Content-Type","application/json");
        httpPost.setHeader(header);
        httpPost.setHeader("Authorization","Basic Og== ");
        httpPost.setHeader("Connection","keep-alive");
        httpPost.setEntity(stringEntity);
        CloseableHttpResponse execute;
        try {
            execute = aDefault.execute(httpPost);
            String str = EntityUtils.toString(execute.getEntity());
//            String images =jsonObject.getString("images");
//            String url= aliUploadUtils.uploadBase64(images,FileEnum.PAINTING.getDec());
            String url= aliUploadUtils.uploadBase64(Objects.requireNonNull(JSONObject.parseObject(str)).getJSONArray("images").getString(0), FileEnum.PAINTING.getDec());
            final Drawing drawing = new Drawing()
                    .setPrompt(dto.getPrompt())
                    .setUserId(currentLoginId)
                    .setEnv(dto.getEnv())
                    .setCreatedTime(LocalDateTime.now());
            drawing.setGenerateUrl(url);
            drawing.setIsPublic(dto.getIs_public());
            userMapper.updateFrequencyById(currentLoginId);
            drawingMapper.insert(drawing);
            return url;
//            List<String> list =JSONObject.parseArray(images,String.class);
//            String image = list.get(0);
//            String name =  LocalDateTime.now().toString().replace(":","-").replace(".","-");
//            convertBase64StrToImage(image,name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *  获取当前登录用户的待审核的图纸列表
     * @return
     */
    @Override
    public List<String> getSdDrawList() {
        return drawingMapper.selectList(new QueryWrapper<Drawing>()
                .lambda()
                .eq(Drawing::getIsPublic, 1)
                .select(Drawing::getGenerateUrl)
        ).stream().map(Drawing::getGenerateUrl).toList();
    }

    /**
     * 获取个人绘画列表
     * @return
     */
    @Override
    public List<DrawingVo> getSdDrawListPrivate() {
        List<DrawingVo> list =  drawingMapper.selectList(new QueryWrapper<Drawing>()
                .lambda()
                .eq(Drawing::getUserId, UserUtils.getCurrentLoginId())
                .select(Drawing::getGenerateUrl,Drawing::getDrawingId)
        ).stream().map(drawing -> {
            DrawingVo drawingVo = new DrawingVo()
                    .setDrawingId(drawing.getDrawingId())
                    .setGenerateUrl(drawing.getGenerateUrl());
            return drawingVo;
        }).collect(Collectors.toList());;
        return list;
    }

    @Override
    public void setPublic(String url) {
        drawingMapper.setPublic(url);
    }

    @Override
    public void deleteDraw(Long id) {
        String filePath = drawingMapper.selectById(id).getGenerateUrl();
        aliUploadUtils.deleteFile(filePath);
        drawingMapper.deleteById(id);
    }

    @Override
    public String checkSdConnectivity() {
        return redisUtils.getValue(ServerConstant.SD_BUTTON).toString();
    }

}
