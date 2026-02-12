package com.cn.bdth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cn.bdth.dto.ProjectDto;
import com.cn.bdth.entity.ProjectField;
import com.cn.bdth.vo.ProjectVo;

import java.util.List;

public interface ProjectService {
    Page<ProjectVo> projectList(Integer pageNum, String projectName, String city, String projectFieldId, String ageRequirement,String status);

    List<ProjectField> fieldList();

    void createProject(ProjectDto projectDto);

    ProjectVo projectDetail(Long projectId);

    Page<ProjectVo> projectListByUserId(Integer pageNum, Long userId);

    void updateProjectStatus(Long projectId, String status);

    void memberStatus(Long projectId, String status, Long userId);

    void join(Long projectId,Boolean isNick);

    Page<ProjectVo> JoinedProjectList(Integer pageNum, Long userId);

    void leave(Long projectId);

    void disband(Long projectId);

}
