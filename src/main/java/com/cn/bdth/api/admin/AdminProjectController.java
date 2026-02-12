package com.cn.bdth.api.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.cn.bdth.msg.Result;
import com.cn.bdth.service.ProjectService;

@RequiredArgsConstructor
@RequestMapping("/admin/project")
@RestController
@Slf4j
public class AdminProjectController {

    private final ProjectService projectService;

    @GetMapping("/list")
    public Result projectList(@RequestParam Integer pageNum,
                              @RequestParam String projectName,
                              @RequestParam String city,
                              @RequestParam String projectFieldId,
                              @RequestParam String ageRequirement,
                              @RequestParam String status) {
        return Result.data(projectService.projectList(pageNum, projectName, city, projectFieldId, ageRequirement, status));
    }

    @PostMapping("/updateStatus")
    public Result updateStatus(@RequestParam Long projectId, @RequestParam String status) {
        try {
            projectService.updateProjectStatus(projectId, status);
            return Result.ok();
        }catch (Exception e){
            return Result.error(e.getMessage());
        }

    }


}
