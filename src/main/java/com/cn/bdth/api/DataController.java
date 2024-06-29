package com.cn.bdth.api;

import com.alibaba.fastjson.JSONObject;
import com.cn.bdth.enums.FileEnum;
import com.cn.bdth.msg.Result;
import com.cn.bdth.service.DataService;
import com.cn.bdth.vo.DataNormalVo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

@RequestMapping("/data")
@RestController
@Slf4j
@RequiredArgsConstructor
public class DataController {
    private final DataService dataService;


    @GetMapping(value = "/getPrice", name = "获取价格类目数量")
    public Result getPrice() {
        CloseableHttpClient aDefault = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet("http://localhost:5000/priceSort");
        JSONObject param = new JSONObject();
        Header header = new BasicHeader("Content-Type", "application/json");
        CloseableHttpResponse execute;
        try {
            execute = aDefault.execute(httpGet);
//            String str = EntityUtils.toString(execute.getEntity());
//            String img = String.valueOf(JSONObject.parseObject(str).getJSONArray("img"));

            String responseContent = EntityUtils.toString(execute.getEntity(), "UTF-8");

            // 在这里处理 JSON 数据
//            byte[] decodedBytes = java.util.Base64.getDecoder().decode(JSONObject.parseObject(responseContent).getString("img"));
//
//            // 创建临时文件
//            File tempFile = File.createTempFile("temp-image", ".jpg");
//            tempFile.deleteOnExit();

            // 将解码后的字节流写入临时文件
//            try (OutputStream stream = new FileOutputStream(tempFile)) {
//                stream.write(decodedBytes);
//            }

            DataNormalVo dataNormalVo = new DataNormalVo()
                    .setImg(JSONObject.parseObject(responseContent).getString("img"))
                    .setTop1(JSONObject.parseObject(responseContent).getString("top1"))
                    .setTop2(JSONObject.parseObject(responseContent).getString("top2"))
                    .setTop3(JSONObject.parseObject(responseContent).getString("top3"))
                    .setValue1(JSONObject.parseObject(responseContent).getString("value1"))
                    .setValue2(JSONObject.parseObject(responseContent).getString("value2"))
                    .setValue3(JSONObject.parseObject(responseContent).getString("value3"));

            return Result.data(dataNormalVo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @GetMapping(value = "/numSort", name = "获取价格类目数量")
    public Result getNum() {
        CloseableHttpClient aDefault = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet("http://localhost:5000/sortNumList");
        JSONObject param = new JSONObject();
        Header header = new BasicHeader("Content-Type", "application/json");
        CloseableHttpResponse execute;
        try {
            execute = aDefault.execute(httpGet);
            String responseContent = EntityUtils.toString(execute.getEntity(), "UTF-8");
            DataNormalVo dataNormalVo = new DataNormalVo()
                    .setImg(JSONObject.parseObject(responseContent).getString("img"))
                    .setTop1(JSONObject.parseObject(responseContent).getString("top1"))
                    .setTop2(JSONObject.parseObject(responseContent).getString("top2"))
                    .setTop3(JSONObject.parseObject(responseContent).getString("top3"))
                    .setValue1(JSONObject.parseObject(responseContent).getString("value1"))
                    .setValue2(JSONObject.parseObject(responseContent).getString("value2"))
                    .setValue3(JSONObject.parseObject(responseContent).getString("value3"));
            return Result.data(dataNormalVo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    @Accessors(chain = true)
    static
    class ShopVo {
        private List<String> numList;
        private List<String> numListValue;
        private List<String> commentsList;
        private List<String> commentsListValue;
    }
    @GetMapping(value = "/shopInfo", name =  "获取店铺信息")
    public Result shopInfo() {
        CloseableHttpClient aDefault = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet("http://localhost:5000/shop");
        JSONObject param = new JSONObject();
        Header header = new BasicHeader("Content-Type", "application/json");
        CloseableHttpResponse execute;
        try {
            execute = aDefault.execute(httpGet);
            String responseContent = EntityUtils.toString(execute.getEntity(), "UTF-8");
            ShopVo shopVo = new ShopVo()
                    .setNumListValue((List<String>) JSONObject.parseObject(responseContent).get("numListValue"));
            return Result.data(shopVo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
