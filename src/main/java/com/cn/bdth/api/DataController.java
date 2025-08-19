package com.cn.bdth.api;

import com.alibaba.fastjson.JSONObject;
import com.cn.bdth.entity.Dialogue;
import com.cn.bdth.msg.Result;
import com.cn.bdth.service.DataService;
import com.cn.bdth.vo.DataNormalVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequestMapping("/data")
@RestController
@Slf4j
@RequiredArgsConstructor
public class DataController {

    private final DataService dataService;

    @GetMapping(value = "/getTimeFrequency/{type}", name = "获取时间频率")
    public Result getTimeFrequency(@PathVariable Integer type) {
        return Result.data(dataService.timeFrequency(type));
    }

    @GetMapping(value = "/getModelFrequency", name = "获取模型使用频率")
    public Result getModelFrequency() {
        return Result.data(dataService.getModelFrequency());
    }

    @GetMapping(value = "/getContentCloud", name = "获取问题词云")
    public Result getContentFrequency() {
        return Result.data(dataService.getWordFrequency(1));
    }

    @GetMapping(value = "/getMessageCloud", name = "获取回答词云")
    public Result getMessageFrequency() {
        return Result.data(dataService.getWordFrequency(2));
    }


}
