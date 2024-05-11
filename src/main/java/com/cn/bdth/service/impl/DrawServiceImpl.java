package com.cn.bdth.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cn.bdth.common.StableDiffusionCommon;
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
import com.cn.bdth.utils.UserUtils;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class DrawServiceImpl implements DrawService {

    private final DrawingMapper drawingMapper;

    private final UserMapper userMapper;

    private final StableDiffusionCommon stableDiffusionCommon;

    private final AliUploadUtils aliUploadUtils;


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


        final SdDrawingModel model = DrawingSdTaskDto.convertToPictureImgModel(dto);
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
        if (dto.getNegative_prompt()!=null){
            param.put("negative_prompt",dto.getNegative_prompt());
        }
        if (dto.getImages()!=null){
            String url= dto.getImages().toString();
            param.put("init_images",url);
            httpPost.setURI(URI.create("https://sd.hepingan.top/sdapi/v1/img2img"));
        }

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
//            return url;
//            List<String> list =JSONObject.parseArray(images,String.class);
//            String image = list.get(0);
//            String name =  LocalDateTime.now().toString().replace(":","-").replace(".","-");
//            convertBase64StrToImage(image,name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Base64字符串转图片
     * @param base64String
     * @param imageFileName
     */
    public static void convertBase64StrToImage(String base64String, String imageFileName) {
        ByteArrayInputStream bais = null;
        try {
            //获取图片类型
            String suffix = imageFileName.substring(imageFileName.lastIndexOf(".") + 1);
            //获取JDK8里的解码器Base64.Decoder,将base64字符串转为字节数组
            byte[] bytes = Base64.getDecoder().decode(base64String);
            //构建字节数组输入流
            bais = new ByteArrayInputStream(bytes);
            //通过ImageIO把字节数组输入流转为BufferedImage
            BufferedImage bufferedImage = ImageIO.read(bais);
            //构建文件
            File imageFile = new File(imageFileName);
            //写入生成文件
            ImageIO.write(bufferedImage, suffix, imageFile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bais != null) {
                    bais.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
    public List<String> getSdDrawListPrivate() {
        List<String> list =  drawingMapper.selectList(new QueryWrapper<Drawing>()
                .lambda()
                .eq(Drawing::getUserId, UserUtils.getCurrentLoginId())
                .select(Drawing::getGenerateUrl)
        ).stream().map(Drawing::getGenerateUrl).toList();
        return list;
    }

    @Override
    public void setPublic(String url) {
        drawingMapper.setPublic(url);
    }

    @Override
    public void deleteDraw(String url) {
        drawingMapper.deleteDraw(url);
    }


}
