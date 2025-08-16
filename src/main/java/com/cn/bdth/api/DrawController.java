package com.cn.bdth.api;

import com.cn.bdth.dto.DrawingSdTaskDto;
import com.cn.bdth.dto.ZhipuDrawDto;
import com.cn.bdth.exceptions.DrawingException;
import com.cn.bdth.exceptions.FrequencyException;
import com.cn.bdth.msg.Result;
import com.cn.bdth.service.DrawService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/draw")
@RequiredArgsConstructor
public class DrawController {

    private final DrawService drawService;


    @PostMapping(value = "/zhipu/image",name = "cogview绘画接口")
    public Result addZhipuDrawingTask(@RequestBody ZhipuDrawDto dto) {
        return Result.data(drawService.addZhipuDrawingTask(dto));
    }
}
