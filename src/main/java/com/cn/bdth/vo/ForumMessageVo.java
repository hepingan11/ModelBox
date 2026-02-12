package com.cn.bdth.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class ForumMessageVo {

    //评论或点赞用户id
    private Long userId;

    //帖子id
    private Long forumId;

    //评论内容
    private String content;

    //帖子图片
    private String forumImg;

    //帖子标题
    private String title;

    private LocalDateTime createdTime;

    //用户名
    private String username;

    //头像
    private String avatar;
}
