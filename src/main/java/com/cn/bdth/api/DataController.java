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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RequestMapping("/data")
@RestController
@Slf4j
@RequiredArgsConstructor
public class DataController {
    private final DataService dataService;

    @GetMapping(value = "/getDialogueData",name = "获取对话数据",produces = MediaType.APPLICATION_JSON_VALUE)
    public Result getDialogueData() {
        List<Dialogue> dialogueList = dataService.getDialogueData();
        return Result.data(dialogueList);
    }

    @GetMapping(value = "/getDialogueData/page",name = "获取对话数据",produces = MediaType.APPLICATION_JSON_VALUE)
    public Result getDialogueDataPage(@RequestParam("no") Integer no,
                                  @RequestParam("size") Integer size) {
        return Result.data(dataService.getDialogueDataPage(no, size));
    }

    @GetMapping(value = "/timeFrequency",name = "按月获取对话频率",produces = MediaType.APPLICATION_JSON_VALUE)
    public Result timeFrequency() {
        return Result.data(dataService.timeFrequency());
    }

    @GetMapping(value = "/getContentCloud", name = "获取问题词云")
    public Result getContentFrequency() {
        return Result.data(dataService.getWordFrequency(1));
    }

    @GetMapping(value = "/getMessageCloud", name = "获取问题词云")
    public Result getMessageFrequency() {
        return Result.data(dataService.getWordFrequency(2));
    }
}
