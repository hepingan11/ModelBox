package com.cn.bdth.api;

import com.cn.bdth.msg.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/draw")
@RequiredArgsConstructor
public class DrawController {

    @PostMapping("/chatv")
    public Result chatv() {
        return Result.data("s");
    }
}
