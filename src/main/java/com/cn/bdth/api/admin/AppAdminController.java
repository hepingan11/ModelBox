package com.cn.bdth.api.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.cn.bdth.msg.Result;
import com.cn.bdth.entity.*;
import com.cn.bdth.mapper.*;
import com.cn.bdth.vo.StatisticsVo;

import java.time.LocalDateTime;
import java.time.LocalTime;

@RequiredArgsConstructor
@RequestMapping("/admin")
@RestController
@Slf4j
public class AppAdminController {

    private final UserMapper userMapper;
    private final ForumMapper forumMapper;
    private final ChatMapper chatMapper;
    private final CarouselMapper carouselMapper;
    private final ChatListMapper chatListMapper;
    private final ReportMapper reportMapper;
    private final ProjectMapper projectMapper;

    @GetMapping("/statistics")
    public Result statistics() {
        Long userCount = userMapper.selectCount(null);
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
        Long todayPostCount = forumMapper.selectCount(new QueryWrapper<Forum>().lambda()
                .ge(Forum::getCreatedTime, startOfDay)
                .le(Forum::getCreatedTime, endOfDay));
        Long projectCount = projectMapper.selectCount(null);
        Long chatCount = chatMapper.selectCount(new QueryWrapper<Chat>().lambda()
                .ge(Chat::getCreatedTime, startOfDay)
                .le(Chat::getCreatedTime, endOfDay));
        StatisticsVo statisticsVo  = new StatisticsVo()
                .setChatCount(chatCount)
                .setForumCount(todayPostCount)
                .setProjectCount(projectCount)
                .setUserCount(userCount);
        return Result.data(statisticsVo);
    }



    @PostMapping("/carousel/add")
    public Result addCarousel(@RequestBody Carousel carousel) {
        carousel.setCreatedTime(LocalDateTime.now());
        if (carousel.getId() != null ){
            return carouselMapper.updateById(carousel) > 0 ? Result.ok() : Result.error();
        }
        return carouselMapper.insert(carousel) > 0 ? Result.ok() : Result.error();
    }

    @GetMapping("/carousel/delete")
    public Result deleteCarousel(@RequestParam Long id) {
        return carouselMapper.deleteById(id) > 0 ? Result.ok() : Result.error();
    }

    @GetMapping("/message/list")
    public Result messageList(@RequestParam Integer pageNum) {
        Page<ChatList> p = new Page<>(pageNum,  10);
        return Result.data(chatListMapper.selectPage(p,null).getRecords());
    }

    @GetMapping("/report/list")
    public Result reportList(@RequestParam Integer pageNum,Integer status) {
        if (status != null){
            return Result.data(reportMapper.selectPage(new Page<>(pageNum,  10),new QueryWrapper<Report>().lambda().eq(Report::getStatus, status)).getRecords());
        }
        Page<Report> p = new Page<>(pageNum,  10);
        return Result.data(reportMapper.selectPage(p,null).getRecords());
    }

    @GetMapping("/report/status")
    public Result reportStatus(@RequestParam Long id) {
        Report report = reportMapper.selectById(id);
        report.setStatus(report.getStatus() == 1 ? 0 : 1);
        return reportMapper.updateById(report) > 0 ? Result.ok() : Result.error();
    }


}
