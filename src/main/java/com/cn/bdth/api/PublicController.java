package com.cn.bdth.api;

import com.alibaba.fastjson.JSONObject;
import com.cn.bdth.msg.Result;
import com.cn.bdth.service.PayService;
import com.cn.bdth.service.ServerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


/**
 * 公开
 *
 * @author  @github dulaiduwang003
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {


    private final ServerService serverService;

    private final PayService payService;

    /**
     * 获取公告
     *
     * @return the announcement
     */
    @GetMapping(value = "/get/announcement", name = "获取公告", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result getAnnouncement() {
        return Result.data(serverService.getAnnouncement());
    }

    /**
     * 支付宝授权
     *
     * @param request the request
     * @return the string
     */
    @PostMapping(value = "/callback/order", name = "支付宝授权回调地址", produces = MediaType.APPLICATION_JSON_VALUE)
    public String alipayPullback(HttpServletRequest request) {
        return payService.alipayPullback(request);
    }

    /**
     * 获取小程序状态
     */
    @GetMapping(value = "/get/wechat/status", name = "获取小程序状态", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result getWechatAppStatus() {
        return Result.data(serverService.wechatStatus());
    }


    @GetMapping(value = "/bilibili/fan",name = "获取b站粉丝")
    public Result getBilibiliFan(){
        String vmid = "443081814";
        String apiUrl = "https://api.bilibili.com/x/relation/stat?vmid=" + vmid;

        CloseableHttpClient aDefault = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(apiUrl);
        CloseableHttpResponse execute;
        try {
            execute = aDefault.execute(httpGet);
            String responseContent = EntityUtils.toString(execute.getEntity(), "UTF-8");
            JSONObject jsonObject = JSONObject.parseObject(responseContent);
            int follower = jsonObject.getJSONObject("data").getIntValue("follower");
            return Result.data(follower);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
