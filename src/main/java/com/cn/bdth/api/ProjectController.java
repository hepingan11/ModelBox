package com.cn.bdth.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.cn.bdth.msg.Result;
import com.cn.bdth.dto.ProjectDto;
import com.cn.bdth.service.ProjectService;
import com.cn.bdth.utils.UserUtils;

@RequiredArgsConstructor
@RequestMapping("/project")
@RestController
@Slf4j
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 获取项目列表
     * @param pageNum
     * @param projectName
     * @param city
     * @param projectFieldId
     * @param ageRequirement
     * @return
     */
    @GetMapping("/list")
    public Result projectList(@RequestParam Integer pageNum,
                              @RequestParam String projectName,
                              @RequestParam String city,
                              @RequestParam String projectFieldId,
                              @RequestParam String ageRequirement) {
        return Result.data(projectService.projectList(pageNum, projectName, city, projectFieldId, ageRequirement,"online"));
    }


    /**
     * 获取项目分类
     * @return
     */
    @GetMapping("/field/list")
    public Result projectFieldList() {
        return Result.data(projectService.fieldList());
    }

    @PostMapping("/create")
    public Result createProject(@RequestBody  ProjectDto projectDto) {
        try {
            projectService.createProject(projectDto);
            return Result.ok();
        }catch (Exception e){
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/detail")
    public Result projectDetail(@RequestParam Long projectId) {
        return Result.data(projectService.projectDetail(projectId));
    }

    @GetMapping("/join")
    public Result join(@RequestParam Long projectId,@RequestParam Boolean isNick) {
        try {
            projectService.join(projectId,isNick);
            return Result.ok();
        }catch (Exception e){
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/projectListByUserId")
    public Result projectListByUserId(@RequestParam Integer pageNum,@RequestParam Long userId) {
        return Result.data(projectService.projectListByUserId(pageNum,userId ));
    }

    @GetMapping("/updateStatus")
    public Result updateStatus(@RequestParam Long projectId,@RequestParam String status) {
        try {
            projectService.updateProjectStatus(projectId, status);
            return Result.ok();
        }catch (Exception e){
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/MyProjectList")
    public Result MyProjectList(@RequestParam Integer pageNum) {
        return Result.data(projectService.projectListByUserId(pageNum, UserUtils.getCurrentLoginId()));
    }

    @PostMapping("/member/status")
    public Result memberStatus(@RequestParam Integer projectId,@RequestParam String status,@RequestParam Long userId) {
        try {
            projectService.memberStatus(Long.valueOf(projectId), status,userId);
            return Result.ok();
        }catch (Exception e){
            return Result.error(e.getMessage());
        }
    }


    @GetMapping("/myJoined")
    public Result myJoin(@RequestParam Integer pageNum) {
        return Result.data(projectService.JoinedProjectList(pageNum, UserUtils.getCurrentLoginId()));
    }

    @GetMapping("/JoinedByUserId")
    public Result JoinedByUserId(@RequestParam Integer pageNum,@RequestParam Long userId) {
        return Result.data(projectService.JoinedProjectList(pageNum,userId));
    }

    @GetMapping("/leave")
    public Result leave(@RequestParam Long projectId) {
        try {
            projectService.leave(projectId);
            return Result.ok();
        }catch (Exception e){
            return Result.error(e.getMessage());
        }
    }

    /**
     * 解散
     */
    @GetMapping("/disband")
    public Result disband(@RequestParam Long projectId) {
        try {
            projectService.disband(projectId);
            return Result.ok();
        }catch (Exception e){
            return Result.error(e.getMessage());
        }
    }

}
