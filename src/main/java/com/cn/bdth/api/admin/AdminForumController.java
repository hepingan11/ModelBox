package com.cn.bdth.api.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.cn.bdth.msg.Result;
import com.cn.bdth.entity.Forum;
import com.cn.bdth.mapper.ForumMapper;
import com.cn.bdth.service.ForumService;
import com.cn.bdth.vo.ForumVo;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/admin/forum")
@RestController
@Slf4j
public class AdminForumController {

    private final ForumMapper forumMapper;

    private final ForumService forumService;

    @GetMapping("/list")
    public Result forumList(@RequestParam Integer pageNum, @RequestParam String title) {
        List<ForumVo> forumVos = forumService.pageTotal(pageNum, title);
        return Result.data(forumVos);
    }

    @GetMapping("/delete")
    public Result delete(@RequestParam Long id) {
        return forumMapper.deleteById(id) > 0 ? Result.ok() : Result.error();
    }

    @GetMapping("/status")
    public Result status(@RequestParam Long id) {
        Forum forum = forumMapper.selectById(id);
        forum.setStatus(forum.getStatus() == 1 ? 0 : 1);
        return forumMapper.updateById(forum) > 0 ? Result.ok() : Result.error();
    }
}
