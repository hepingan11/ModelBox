package com.cn.bdth.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.cn.bdth.msg.Result;
import com.cn.bdth.entity.Report;
import com.cn.bdth.mapper.ReportMapper;
import com.cn.bdth.utils.UserUtils;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/report")
@RestController
@Slf4j
@RequiredArgsConstructor
public class ReportController {

    private final ReportMapper reportMapper;

    @PostMapping("/submit")
    public Result report(@RequestBody Report report){
        report.setUserId(UserUtils.getCurrentLoginId())
                .setStatus(0)
                .setCreatedTime(LocalDateTime.now());
        reportMapper.insert(report);
        return Result.ok();
    }

    @GetMapping("/list")
    public Result list(@RequestParam Integer pageNum){
        List<Report> reportList = reportMapper.selectPage(new Page<>(pageNum,10),new QueryWrapper<Report>().lambda()
                .eq(Report::getUserId, UserUtils.getCurrentLoginId())).getRecords();
        return Result.data(reportList);
    }
}
