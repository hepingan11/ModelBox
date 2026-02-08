package com.cn.bdth.utils;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Component
@Slf4j
public class WechatUtil {
    //	配置自己的app_id、app_secret
    @Value("${wx.appid}")
    private String APP_ID;

    @Value("${wx.appSecret}")
    private String APP_SECRET;

    private final RedisUtils redisUtils;

    public String getOpenId(String loginCode) {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        String requestUrl = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("appid", APP_ID)
                .queryParam("secret", APP_SECRET)
                .queryParam("js_code", loginCode)
                .queryParam("grant_type", "authorization_code")
                .toUriString();

        HttpResponse response = HttpUtil.createGet(requestUrl).execute();

        // 获取 session_key 和 openid
        JSONObject parseObj = JSONUtil.parseObj(response.body());

        return (String) parseObj.get("openid");
    }

    /**
     * 获取access_token
     * @return
     */
    public String getAccessToken(){
        String url = "https://api.weixin.qq.com/cgi-bin/token";
        String requestUrl = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("appid", APP_ID)
                .queryParam("secret", APP_SECRET)
                .queryParam("grant_type", "client_credential")
                .toUriString();
        HttpResponse response = HttpUtil.createGet(requestUrl).execute();

        // 获取 session_key 和 openid
        JSONObject parseObj = JSONUtil.parseObj(response.body());
        redisUtils.setValue("access_token", (String)parseObj.get("access_token"));
        redisUtils.expire("access_token", 7200);
        return (String)parseObj.get("access_token");
    }

    /**
     * 文本内容检测
     * Scene场景枚举值（1 资料；2 评论；3 论坛；4 社交日志）
     * @param content
     * @return
     */

    public String textCheck(String content,Integer scene,String openId){
        String url = "https://api.weixin.qq.com/wxa/msg_sec_check";
        String accessToken = (String) redisUtils.getValue("access_token");
        if (accessToken == null){
            accessToken = getAccessToken();
        }
        String requestUrl = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("access_token", accessToken)
                .toUriString();
        String jsonBody = JSONUtil.createObj()
                .put("content", content)
                .put("version", 2)
                .put("scene", scene)
                .put("openid", openId)
                .toString();
        HttpResponse response = HttpUtil.createPost(requestUrl)
                .body(jsonBody)
                .execute();

        // 获取响应内容并解析
        JSONObject parseObj = JSONUtil.parseObj(response.body());
        String errmsg = (String) parseObj.get("errmsg");
        if (!"ok".equals(errmsg)){
            return errmsg;
        }
        JSONObject  result = JSONUtil.parseObj(parseObj.get("result"));
        int label = ((Number) result.get("label")).intValue();
        return String.valueOf(label);
        //命中标签枚举值，100 正常；10001 广告；20001 时政；20002 色情；20003 辱骂；20006 违法犯罪；20008 欺诈；20012 低俗；20013 版权；21000 其他
    }

    /**
     * 图片内容检测
     * @param imgUrl
     * @param scene
     * @param openId
     * @return
     */
    public String mediaCheck(String imgUrl,Integer scene,String openId){
        String url = "https://api.weixin.qq.com/wxa/msg_sec_check";
        String accessToken = (String) redisUtils.getValue("access_token");
        if (accessToken == null){
            accessToken = getAccessToken();
        }
        String requestUrl = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("access_token", accessToken)
                .toUriString();
        String jsonBody = JSONUtil.createObj()
                .put("media_url", imgUrl)
                .put("media_type", 2)
                .put("version", 2)
                .put("scene", scene)
                .put("openid", openId)
                .toString();
        HttpResponse response = HttpUtil.createPost(requestUrl)
                .body(jsonBody)
                .execute();

        JSONObject parseObj = JSONUtil.parseObj(response.body());
        String errmsg = (String) parseObj.get("errmsg");
        if (!"ok".equals(errmsg)){
            //返回错误信息
            return errmsg;
        }else {
            //返回trace_id
            return (String) parseObj.get("trace_id");
        }

    }

}