package com.cn.bdth.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVo {
    private Long id;

    private Long forumId;

    private Long userId;

    private String content;

    private Integer likes;

    private Integer isParent;

    private Long parentId;

    private String img;

    private String username;

    private String avatar;

    private String avatarFrameUrl;

    private Integer level;

    private Boolean isLiked;

    private LocalDateTime createdTime;
}
