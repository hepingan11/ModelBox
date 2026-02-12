package com.cn.bdth.api.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.cn.bdth.msg.Result;
import com.cn.bdth.entity.Activity;
import com.cn.bdth.mapper.ActivityMapper;
import com.cn.bdth.utils.AliUploadUtils;
import com.cn.bdth.utils.UserUtils;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RequestMapping("/admin/activity")
@RestController
@Slf4j
public class AdminActivityController {

    private final ActivityMapper activityMapper;

    private final AliUploadUtils aliUploadUtils;

    @GetMapping("/page")
    public Result activityPage(@RequestParam Integer pageNum, @RequestParam String title, @RequestParam String type) {
        Page<Activity> p = new Page<>(pageNum,  10);
        QueryWrapper<Activity> activityQueryWrapper = new QueryWrapper<>();
        if (title != null && !title.isEmpty()){
            activityQueryWrapper.lambda().like(Activity::getTitle, title);
        }
        if (type != null && !type.isEmpty()){
            activityQueryWrapper.lambda().eq(Activity::getType, type);
        }
        return Result.data(activityMapper.selectPage(p,activityQueryWrapper).getRecords());
    }

    @PostMapping("/add")
    public Result addActivity(@RequestBody Activity activity) {
        activity.setCreatedTime(LocalDateTime.now());
        activity.setUserId(UserUtils.getCurrentLoginId());
        activityMapper.insert(activity);
        return Result.ok();
    }

    @PostMapping("/update")
    public Result update(@RequestBody Activity activity){
        return activityMapper.updateById(activity) > 0 ? Result.ok() : Result.error();
    }

    @PostMapping("/delete")
    public Result update(@RequestBody Long id){
        aliUploadUtils.deleteFile(activityMapper.selectById(id).getImage());
        return activityMapper.deleteById(id) > 0 ? Result.ok() : Result.error();
    }
}
