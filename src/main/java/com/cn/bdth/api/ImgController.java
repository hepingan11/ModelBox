package com.cn.bdth.api;

import com.cn.bdth.dto.TokenRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Slf4j
@RequestMapping("/img")
@RequiredArgsConstructor
public class ImgController {
    @PostMapping("/api/token")
    public String getToken(@RequestBody TokenRequest tokenRequest) throws IOException {
        // 获取请求参数
        String email = tokenRequest.getEmail();
        String password = tokenRequest.getPassword();
        Integer refresh = tokenRequest.getRefresh();

        // 构建HTTP请求
        String url = "https://www.imgtp.com/api/token";
        HttpPost httpPost = new HttpPost(url);
        StringEntity requestEntity = new StringEntity("{\"email\": \"" + email + "\", \"password\": \"" + password + "\", \"refresh\": " + refresh + "}", ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);

        // 发送请求并获取响应
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity);
            } else {
                return null;
            }
        }
    }
}
