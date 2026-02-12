package com.cn.bdth.vo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ForumVo {

    private Long id;

    private String title;

    private String content;

    private Long userId;

    private String username;

    private Integer level;

    private String avatar;

    private String avatarFrameUrl;

    private Integer look;

    private Integer likes;

    private Integer status;

    private Boolean isLiked;

    private LocalDateTime createdTime;

    private List<String> imageUrl;

    private List<MultipartFile> image;
}
