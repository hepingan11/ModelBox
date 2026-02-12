package com.cn.bdth.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ProjectDto {

    private String projectName;

    private String introduce;

    private String city;

    private String ageRequirement;

    // 项目分类id
    private Long projectFieldId;

    // 图片
    private List<String> imageUrlList;

}
