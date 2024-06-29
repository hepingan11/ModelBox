package com.cn.bdth.api;

import com.cn.bdth.msg.Result;
import com.cn.bdth.service.PassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequestMapping("/pass")
@RestController
@Slf4j
@RequiredArgsConstructor
public class PassController {
    private final PassService passService;


    @GetMapping(value = "/getNews/{page}",name = "获取新闻")
    public Result getNewsList(@PathVariable("page") Integer page){
        return Result.data(passService.getNews(page));
    }

    @GetMapping(value = "getAllNum",name = "获取总数据")
    public Result getAllNum(){
        CloseableHttpClient aDefault = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet("http://localhost:5000/getNum");
        CloseableHttpResponse execute;
        try {
            execute = aDefault.execute(httpGet);
            String responseContent = EntityUtils.toString(execute.getEntity(), "UTF-8");
            return Result.data(Integer.parseInt(responseContent));
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    @GetMapping(value = "/login/student",name = "学生登录",produces = MediaType.APPLICATION_JSON_VALUE)
    public Result login(@RequestParam("account") String account,@RequestParam("password") String password){
        if (passService.login(account,password) !=null){
            return Result.data(passService.login(account,password));
        }else {
            return Result.error("账号或密码错误");
        }
    }
}
