package com.cn.bdth.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cn.bdth.api.DataController;
import com.cn.bdth.msg.Result;
import com.cn.bdth.service.PassService;
import com.cn.bdth.vo.NewsVo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PassServiceImpl implements PassService {

    @Override
    public List<NewsVo> getNews(Integer page) {
        CloseableHttpClient aDefault = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet("http://localhost:5000/getNews/"+page);
        CloseableHttpResponse execute;
        try {
            execute = aDefault.execute(httpGet);
            String responseContent = EntityUtils.toString(execute.getEntity(), "UTF-8");

            Gson gson = new Gson();
            Type doListType = new TypeToken<List<NewsVo>>(){}.getType();
            List<NewsVo> doList = gson.fromJson(responseContent, doListType);
            return doList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String login(String account, String password) {
        CloseableHttpClient aDefault = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet("http://localhost:5000/login?account="+account+"&password="+password);
        CloseableHttpResponse execute;
        try {
            execute = aDefault.execute(httpGet);
            String responseContent = EntityUtils.toString(execute.getEntity(), "UTF-8");
            return responseContent;
        }catch (IOException e){
            throw new RuntimeException(e);
        }

    }
}
