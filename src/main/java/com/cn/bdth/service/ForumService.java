package com.cn.bdth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cn.bdth.entity.Comment;
import com.cn.bdth.entity.Forum;
import com.cn.bdth.vo.CommentVo;
import com.cn.bdth.vo.ForumMessageVo;
import com.cn.bdth.vo.ForumVo;

import java.util.List;

public interface ForumService extends IService<Forum> {

    List<ForumVo> pageBynum(Integer pageNum, String title, Long userId);

    void publish(ForumVo forumVo);

    List<CommentVo> getComment(Long id);

    String comment(Comment comment);

    void commentLike(Long id);

    List<ForumVo> pageMyLike(Integer pageNum);

    List<ForumVo> weeklyHot();

    List<ForumVo> monthlyHot();

    List<ForumVo> pageTotal(Integer pageNum, String title);

    Page<ForumMessageVo> messageLikes(Integer pageNum);

    Page<ForumMessageVo> messageComments(Integer pageNum);
}
