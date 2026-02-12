package com.cn.bdth.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.cn.bdth.msg.Result;
import com.cn.bdth.entity.Activity;
import com.cn.bdth.mapper.ActivityMapper;

@RequiredArgsConstructor
@RequestMapping("/activity")
@RestController
@Slf4j
public class ActivityController {

    private final ActivityMapper activityMapper;
    @GetMapping("/list")
    public Result list(@RequestParam Integer pageNum){
        Page<Activity> p = new Page<>(pageNum,  5);
        LambdaQueryWrapper<Activity> lambdaQueryWrapper = new QueryWrapper<Activity>().lambda()
                .eq(Activity::getStatus, "上线")
                .or()
                .eq(Activity::getStatus, "hot")
                .orderByDesc(Activity::getCreatedTime);
        return Result.data(activityMapper.selectPage(p,lambdaQueryWrapper).getRecords());
    }

    @PostMapping("/add")
    public Result add(@RequestBody Activity activity){
        return activityMapper.insert(activity) > 0 ? Result.ok() : Result.error();
    }

    @PostMapping("/delete")
    public Result delete(@RequestBody Long id){
        return activityMapper.deleteById(id) > 0 ? Result.ok() : Result.error();
    }

    @GetMapping("/getById")
    public Result getById(@RequestParam Long id){
        return Result.data(activityMapper.selectById(id));
    }

    @GetMapping("/hotList")
    public Result hotList(){
        return Result.data(activityMapper.selectList(new QueryWrapper<Activity>().lambda()
                .eq(Activity::getStatus, "hot")
                .orderByDesc(Activity::getCreatedTime)));
    }
}
