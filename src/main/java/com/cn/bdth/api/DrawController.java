package com.cn.bdth.api;

import com.cn.bdth.dto.DrawingSdTaskDto;
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

    @PostMapping(value = "/postSdDraw",name = "提交绘画请求", consumes = "multipart/form-data")
    public Result postSdDraw(@Valid DrawingSdTaskDto dto) {
        try {
            return Result.data(drawService.addSdTask(dto));
        }catch (FrequencyException | DrawingException e){
            return Result.error(e.getMessage());
        }
    }

    @GetMapping(value = "/getSdDrawList/public",name = "获取公共绘画请求列表", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result getSdDrawListPublic() {
        return Result.data(drawService.getSdDrawList());
    }

    @GetMapping(value = "/getSdDrawList/private",name = "获取个人绘画请求列表", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result getSdDrawListPrivate() {
        return Result.data(drawService.getSdDrawListPrivate());
    }

    @PostMapping(value = "/setPublic/{url}",name = "设置图片为公共", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result setPublic(@PathVariable String url) {
        drawService.setPublic(url);
        return Result.ok();
    }

    @PostMapping(value = "/deleteDraw/{url}",name = "删除绘画请求", consumes = "application/json" )
    public Result deleteDraw(@PathVariable String url) {
        drawService.deleteDraw(url);
        return Result.ok();
    }
}
