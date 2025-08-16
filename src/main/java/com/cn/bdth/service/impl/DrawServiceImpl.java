package com.cn.bdth.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cn.bdth.dto.ZhipuDrawDto;
import com.cn.bdth.enums.FileEnum;
import com.cn.bdth.service.DrawService;
import com.cn.bdth.utils.AliUploadUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
@RequiredArgsConstructor
@Slf4j
public class DrawServiceImpl implements DrawService {

    private final AliUploadUtils aliUploadUtils;


    @Value("${config.glmKey}")
    private String glmKey;

    @Value("${config.doubaokey}")
    private String doubaokey;

    @Value("${config.openKey}")
    private String openKey;

    @Override
    public String addZhipuDrawingTask(ZhipuDrawDto dto) {
        // 设置请求的URL地址
        CloseableHttpClient aDefault = HttpClients.createDefault();

        String url ="";
        String key = "";

        switch (dto.getModel()){
            case "doubao-seedream-3-0-t2i-250415":
                url = "https://ark.cn-beijing.volces.com/api/v3/images/generations";
                key = doubaokey;
                break;
            case "gpt-image-1":
                url = "https://sg.uiuiapi.com/v1/images/generations";
                key = openKey;
                break;
            case "cogview-4-250304":
                url = "https://open.bigmodel.cn/api/paas/v4/images/generations";
                key = glmKey;
                break;
        }
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Authorization", "Bearer " +key);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(JSON.toJSONString(dto), ContentType.APPLICATION_JSON));
        CloseableHttpResponse execute;
        try {
            execute = aDefault.execute(httpPost);
            String str = EntityUtils.toString(execute.getEntity());
            // 解析 JSON 字符串
            JSONObject jsonObject = JSONObject.parseObject(str);
            if (dto.getModel().equals("gpt-image-1")){
                String base64 = jsonObject.getJSONArray("data")
                        .getJSONObject(0)
                        .getString("b64_json");
                return aliUploadUtils.uploadBase64(base64, FileEnum.PAINTING.getDec());
            }else {
                return jsonObject.getJSONArray("data")
                        .getJSONObject(0)
                        .getString("url");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "请求出错";
        }

    }

}
