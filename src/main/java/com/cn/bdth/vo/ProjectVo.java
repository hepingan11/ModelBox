package com.cn.bdth.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class ProjectVo {

    private Long projectId;

    private String projectName;

    private String introduce;

    private Long userId;

    private LocalDateTime createdTime;

    //pending-view(待审核)，online(通过且已上线)，not-online(通过但未上线),not-view(未通过)
    private String status;

    private String failReason;

    private String city;

    private String ageRequirement;

    // 项目分类id
    private Long projectFieldId;

    // 项目分类名称
    private String projectFieldName;

    // 图片
    private List<String> imageUrlList;

    // 成员
    private List<ProjectMemberVo> memberList;


    @Data
    @Accessors(chain = true)
    public static
    class ProjectMemberVo {

        private Long id;

        private String username;

        private String avatar;

        //用户等级
        private Integer level;

        //头像框
        private String avatarFrameUrl;

        private Boolean isNick;

        //review通过，reject拒绝,pending待审核
        private String status;

        //加入时间
        private LocalDateTime createdTime;

        //审核失败原因
        private String failReason;
    }
}
