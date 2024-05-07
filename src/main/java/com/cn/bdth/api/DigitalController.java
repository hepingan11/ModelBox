package com.cn.bdth.api;

import com.cn.bdth.dto.DigitalChatDto;
import com.cn.bdth.dto.DigitalVideoDto;
import com.cn.bdth.msg.Result;
import com.cn.bdth.service.DigitalService;
import com.cn.bdth.vo.DigitalChatVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/digital")
@RequiredArgsConstructor
public class DigitalController {

    private final DigitalService digitalService;

    @GetMapping(value = "/getVideoList",name = "获取视频列表",produces = MediaType.APPLICATION_JSON_VALUE)
    public Result getVideoList() {
        return Result.data(digitalService.getVideoList());
    }

    @PostMapping(value = "/addModel", name = "新增数字人模型", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result addModel(@RequestBody @Validated DigitalVideoDto dto){
        digitalService.addModel(dto);
        return Result.ok();
    }

    @PostMapping(value = "/deleteModel", name = "删除数字人模型",produces = MediaType.APPLICATION_JSON_VALUE)
    public Result deleteModel(@PathVariable final long id){
        digitalService.deleteModel(id);
        return Result.ok();
    }

    @PostMapping(value = "/updateModel", name = "更新数字人模型",produces = MediaType.APPLICATION_JSON_VALUE)
    public Result updateModel(@RequestBody @Validated DigitalVideoDto dto){
        digitalService.updateModel(dto);
        return Result.ok();
    }

    @PostMapping(value = "chat", name = "模型对话",produces = MediaType.APPLICATION_JSON_VALUE)
    public Result chatModel(@RequestBody @Validated DigitalChatDto dto){
        return Result.data(digitalService.chatModel(dto));
    }
}
