package com.cn.bdth.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.bdth.common.StableDiffusionCommon;
import com.cn.bdth.dto.DrawingSdTaskDto;
import com.cn.bdth.entity.Drawing;
import com.cn.bdth.enums.FileEnum;
import com.cn.bdth.mapper.DrawingMapper;
import com.cn.bdth.model.SdDrawingModel;
import com.cn.bdth.service.DrawService;
import com.cn.bdth.utils.AliUploadUtils;
import com.cn.bdth.utils.UserUtils;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class DrawServiceImpl implements DrawService {

    private final DrawingMapper drawingMapper;

    private final RedisTemplate redisTemplate;

    private final StableDiffusionCommon stableDiffusionCommon;


    /**
     * 发送绘画请求
     * @param dto
     * @return
     */
    @Override
    public String addSdTask(final DrawingSdTaskDto dto) {
        //获取当前登录用户的ID
        final Long currentLoginId = UserUtils.getCurrentLoginId();
        int isType = 0;
        final Drawing drawing = new Drawing()
                .setPrompt(dto.getPrompt())
                .setUserId(currentLoginId)
                .setEnv(dto.getEnv())
                .setCreatedTime(LocalDateTime.now());

        final SdDrawingModel model = DrawingSdTaskDto.convertToPictureImgModel(dto);
        drawingMapper.insert(drawing);
        // 设置请求的URL地址
        CloseableHttpClient aDefault = HttpClients.createDefault();

        HttpPost httpPost =new HttpPost("https://sd.hepingan.top/sdapi/v1/txt2img");
        JSONObject param = new JSONObject();
        param.put("prompt", dto.getPrompt());
        param.put("step",dto.getSteps());
        param.put("height",dto.getHeight());
        param.put("width",dto.getWidth());
        param.put("sampler_name",dto.getSampler_index());
        JSONObject seed = new JSONObject();
        seed.put("sd_model_checkpoint",dto.getModelName());
        param.put("override_settings",seed);

        StringEntity stringEntity= null;
        try {
            stringEntity = new StringEntity(param.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        Header header=new BasicHeader("Content-Type","application/json");
        httpPost.setHeader(header);
        httpPost.setHeader("Authorization","Basic Og== ");
        httpPost.setHeader("Connection","keep-alive");
        httpPost.setEntity(stringEntity);
        CloseableHttpResponse execute;
        try {
            execute = aDefault.execute(httpPost);
            String s = EntityUtils.toString(execute.getEntity());
            return s;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        AliUploadUtils aliUploadUtils = new AliUploadUtils();

//        String imageUri = null;
//        try {
//            s = EntityUtils.toString(execute.getEntity());
//            imageUri = aliUploadUtils.uploadBase64(Objects.requireNonNull(JSONObject.parseObject(s)).getJSONArray("images").getString(0), FileEnum.PAINTING.getDec());
//            log.info("绘画成功：{}",imageUri);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

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
    public List<String> getSdDrawListPrivate() {
        return drawingMapper.selectList(new QueryWrapper<Drawing>()
                .lambda()
                .eq(Drawing::getUserId, UserUtils.getCurrentLoginId())
                .select(Drawing::getGenerateUrl)
        ).stream().map(Drawing::getGenerateUrl).toList();
    }


}
