package com.cn.bdth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cn.bdth.constants.ProjectStatusConstant;
import com.cn.bdth.dto.ProjectDto;
import com.cn.bdth.entity.*;
import com.cn.bdth.mapper.*;
import com.cn.bdth.service.ProjectService;
import com.cn.bdth.utils.AliUploadUtils;
import com.cn.bdth.utils.BeanUtils;
import com.cn.bdth.utils.UserUtils;
import com.cn.bdth.vo.ProjectVo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectMapper projectMapper;
    private final ProjectImagesMapper projectImagesMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final UserMapper userMapper;
    private final ProjectFieldMapper projectFieldMapper;
    private final AliUploadUtils aliUploadUtils;

    @Override
    public Page<ProjectVo> projectList(Integer pageNum, String projectName, String city, String projectFieldId, String ageRequirement,String status) {
        LambdaQueryWrapper<Project> queryWrapper = new QueryWrapper<Project>().lambda()
                .orderByDesc(Project::getCreatedTime);
        if (status != null && !status.isEmpty()){
            queryWrapper.eq(Project::getStatus, status);
        }
        Page<Project> p = new Page<>(pageNum,  10);
        if (projectName != null && !projectName.isEmpty()){
            queryWrapper.like(Project::getProjectName, projectName);
        }
        if (city != null && !city.isEmpty()){
            if (city. contains(",")){
                String[] split = city.split(",");
                for (String s : split){
                    queryWrapper.like(Project::getCity, s).or();
                }
            }else {
                queryWrapper.like(Project::getCity, city);
            }
        }
        if (projectFieldId != null && !projectFieldId.equals("0")){
            String[] split = projectFieldId.toString().split(",");
            for (String s : split){
                Long id = Long.valueOf(s);
                if (projectFieldMapper.selectById(id).getParentId() ==null){
                        List<Long> sonProjectFieldIdList = projectFieldMapper.selectList(new QueryWrapper<ProjectField>().lambda()
                                    .eq(ProjectField::getParentId, id))
                            .stream().map(ProjectField::getProjectFieldId).toList();
                    for (Long sonProjectFieldId : sonProjectFieldIdList){
                        queryWrapper.eq(Project::getProjectFieldId, sonProjectFieldId).or();
                    }
                }
                queryWrapper.eq(Project::getProjectFieldId, id).or();
            }

        }
        if (ageRequirement != null && !ageRequirement.isEmpty()){
            queryWrapper.eq(Project::getAgeRequirement, ageRequirement);
        }

        Page<Project> projectPage = projectMapper.selectPage(p, queryWrapper);
        Page<ProjectVo> projectVoPage = BeanUtils.copyClassProperTies(projectPage, Page.class);
        List<ProjectVo> projectVoList = new ArrayList<>();
        for (Project project : projectPage.getRecords()){
            projectVoList.add(getProjectVo(project));
        }
        projectVoPage.setRecords(projectVoList);
        return projectVoPage;
    }

    @Override
    public List<ProjectField> fieldList() {
        return projectFieldMapper.selectList(null);
    }

    @Override
    @Transactional
    public void createProject(ProjectDto projectDto) {
        if(projectMapper.selectList(new QueryWrapper<Project>().lambda()
                .eq(Project::getUserId, UserUtils.getCurrentLoginId())).size()==10){
            throw new RuntimeException("项目数量(10)已达上限,如需添加请联系管理员");
        }
        Project project = new Project().setUserId(UserUtils.getCurrentLoginId())
                .setProjectName(projectDto.getProjectName())
                .setIntroduce(projectDto.getIntroduce())
                .setCity(projectDto.getCity())
                .setAgeRequirement(projectDto.getAgeRequirement())
                .setProjectFieldId(projectDto.getProjectFieldId())
                .setStatus(ProjectStatusConstant.PENDING)
                .setCreatedTime(LocalDateTime.now());
        projectMapper.insert(project);
        for (String imageUrl : projectDto.getImageUrlList()){
            projectImagesMapper.insert(new ProjectImages().setProjectId(project.getProjectId())
                    .setImageUrl(imageUrl));
        }
    }

    @Override
    public ProjectVo projectDetail(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        return getProjectVo(project);
    }

    @Override
    public Page<ProjectVo> projectListByUserId(Integer pageNum, Long userId) {
        Page<Project> page = new Page<>(pageNum, 10);
        LambdaQueryWrapper<Project> queryWrapper = new QueryWrapper<Project>().lambda()
                .eq(Project::getUserId, userId)
                .orderByDesc(Project::getCreatedTime);
        if (!Objects.equals(userId, UserUtils.getCurrentLoginId())){
            queryWrapper.eq(Project::getStatus, ProjectStatusConstant.ONLINE);
        }
        Page<Project> projectPage = projectMapper.selectPage( page,queryWrapper);
        Page<ProjectVo> projectVoPage = BeanUtils.copyClassProperTies(projectPage, Page.class);
        List<ProjectVo> list = projectPage.getRecords().stream().map(this::getProjectVo).toList();
        return projectVoPage.setRecords(list);
    }

    @Override
    public void updateProjectStatus(Long projectId, String status) {
        projectMapper.update(new Project().setStatus(status), new QueryWrapper<Project>().lambda()
                .eq(Project::getProjectId, projectId));
    }

    @Override
    public void memberStatus(Long projectId, String status, Long userId) {
        projectMemberMapper.update(new ProjectMember().setStatus(status), new QueryWrapper<ProjectMember>().lambda()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId));
    }

    /**
     * 加入项目
     * @param projectId
     */
    @Override
    public void join(Long projectId,Boolean isNick) {
        if (projectMemberMapper.exists(new QueryWrapper<ProjectMember>().lambda()
                .eq(ProjectMember::getProjectId, projectId))){
            throw new RuntimeException("您已加入该项目");
        }
        if (Objects.equals(projectMapper.selectById(projectId).getUserId(), UserUtils.getCurrentLoginId())){
            throw new RuntimeException("您不能加入自己的项目");
        }
        projectMemberMapper.insert(new ProjectMember().setProjectId(projectId)
                .setUserId(UserUtils.getCurrentLoginId())
                .setIsNick(isNick)
                .setStatus(ProjectStatusConstant.PENDING)
                .setCreatedTime(LocalDateTime.now()));
    }

    @Override
    public Page<ProjectVo> JoinedProjectList(Integer pageNum, Long userId) {
        Page<ProjectMember> p = new Page<>(pageNum, 10);
        LambdaQueryWrapper<ProjectMember> queryWrapper = new QueryWrapper<ProjectMember>().lambda()
                .eq(ProjectMember::getUserId, userId);
        if (!Objects.equals(userId, UserUtils.getCurrentLoginId())){
            queryWrapper.eq(ProjectMember::getStatus,ProjectStatusConstant.ONLINE);
        }
        Page<ProjectMember> projectMemberPage = projectMemberMapper.selectPage(p, queryWrapper);
        if (projectMemberPage.getTotal() == 0){
            return new Page<>();
        }
        List<Long> list = projectMemberPage.getRecords().stream().map(ProjectMember::getProjectId).toList();
        Page<ProjectVo> page = BeanUtils.copyClassProperTies(projectMemberPage, Page.class);
        return page.setRecords(projectMapper.selectList(new QueryWrapper<Project>().lambda()
                        .in(Project::getProjectId, list))
                .stream().map(this::getProjectVo).toList());
    }

    @Override
    public void leave(Long projectId) {
        projectMemberMapper.delete(new QueryWrapper<ProjectMember>().lambda()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, UserUtils.getCurrentLoginId()));
    }

    @Override
    public void disband(Long projectId) {
        if (!Objects.equals(projectMapper.selectById(projectId).getUserId(), UserUtils.getCurrentLoginId())){
            throw new RuntimeException("您没有权限");
        }
        projectMemberMapper.delete(new QueryWrapper<ProjectMember>().lambda()
                .eq(ProjectMember::getProjectId, projectId));
        List<String> list = projectImagesMapper.selectList(new QueryWrapper<ProjectImages>().lambda()
                .eq(ProjectImages::getProjectId, projectId)).stream().map(ProjectImages::getImageUrl).toList();
        for (String s : list){
            aliUploadUtils.deleteFile(s);
        }
        projectImagesMapper.delete(new QueryWrapper<ProjectImages>().lambda()
                .eq(ProjectImages::getProjectId, projectId));
        projectMapper.delete(new QueryWrapper<Project>().lambda()
                .eq(Project::getProjectId, projectId)
                .eq(Project::getUserId, UserUtils.getCurrentLoginId()));
    }

    //获取项目详情
    private ProjectVo getProjectVo(Project project) {
        ProjectVo projectVo = BeanUtils.copyClassProperTies(project, ProjectVo.class);
        List<ProjectImages> projectImages = projectImagesMapper.selectList(new QueryWrapper<ProjectImages>().lambda()
                .eq(ProjectImages::getProjectId, project.getProjectId()));
        projectVo.setImageUrlList(projectImages.stream().map(ProjectImages::getImageUrl).toList());
        LambdaQueryWrapper<ProjectMember> queryWrapper = new QueryWrapper<ProjectMember>().lambda()
                .eq(ProjectMember::getProjectId, project.getProjectId());
        Long userId = 0L;
        try {
            userId = UserUtils.getCurrentLoginId();
        }catch (Exception ignored){}

        if (!Objects.equals(userId, project.getUserId())){
            queryWrapper.eq(ProjectMember::getStatus, ProjectStatusConstant.ONLINE);
        }else {
            queryWrapper.eq(ProjectMember::getStatus, ProjectStatusConstant.PENDING).or()
                    .eq(ProjectMember::getStatus, ProjectStatusConstant.ONLINE);
        }
        List<ProjectMember> projectMembers = projectMemberMapper.selectList(queryWrapper);
        if (!projectMembers.isEmpty()){
            List<ProjectVo.ProjectMemberVo> userList = new ArrayList<>();
            for (ProjectMember projectMember : projectMembers){
                if (projectMember.getIsNick() && !(Objects.equals(projectMember.getUserId(), userId) || (Objects.equals(project.getUserId(), userId)))){
                    userList.add(new ProjectVo.ProjectMemberVo().setUsername("匿名用户")
                            .setStatus(projectMember.getStatus())
                            .setCreatedTime(projectMember.getCreatedTime())
                            .setAvatar("https://img-hepingan.oss-cn-hangzhou.aliyuncs.com/page1/%E5%8C%BF%E5%90%8D.png"));
                } else {
                    User user = userMapper.selectById(projectMember.getUserId());
                    userList.add(BeanUtils.copyClassProperTies(user, ProjectVo.ProjectMemberVo.class)
                            .setCreatedTime(projectMember.getCreatedTime())
                            .setStatus(projectMember.getStatus()));
                }
            }
            projectVo.setMemberList(userList);
        }
        ProjectField projectField = projectFieldMapper.selectById(project.getProjectFieldId());
        projectVo.setProjectFieldName(projectFieldMapper.selectById(projectField.getParentId()).getFieldName()+"---"+projectField.getFieldName());
        return projectVo;
    }
}
