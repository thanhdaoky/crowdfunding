package com.dkthanh.demo.controller;

import com.dkthanh.demo.domain.UserDetailEntity;
import com.dkthanh.demo.domain.UserEntity;
import com.dkthanh.demo.domain.dto.ProjectFullInfoEntity;
import com.dkthanh.demo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class SysManagementController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserDetailService userDetailService;

    // test UI
    @GetMapping(value = "/creator/test")
    public String testUI(){
        return "/admin/create-project";
    }

    @GetMapping(value = "/admin/dashboard")
        public String getDashboard(){
            return "admin/dashboard";
        }

    /*
    /creator/project/list
    /creator/create-project
    /creator/project/id
    /creator/project/save
    /creator/project/edit/id
    /creator/project/delete/id
    /creator/team/user
     */

    // creator project/list
    @GetMapping(value = "/creator/project/list" )
    public String getCreatorProjectList(Model model, @PathVariable("creator-id") Integer creatorId, HttpServletRequest request, Authentication authentication){
        String username = null;
        UserDetailEntity userDetailEntity = null;
        UserEntity user = null;
        if(authentication != null) {

            username = ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        if(!"".equals(username)){
            user = userService.findUserByUsername(username);
        }

        Integer userId  = user.getId();

        userDetailEntity = userDetailService.getUserDetailByUserId(userId);

        List<ProjectFullInfoEntity> list = projectService.getProjectListOfCreator(creatorId);
        model.addAttribute("project-list", list);
        model.addAttribute("creator", userDetailEntity);
        return "/admin/creator-project-list";
    }

    // creator project/id
    @GetMapping(value = "/creator/project/{id}")
    public String getProjectDetail(Model model, @PathVariable("id") Integer projectId, HttpServletRequest request, Authentication authentication){
        ProjectFullInfoEntity p = projectService.getProjectDetail(projectId);
        model.addAttribute("project", p);
        return "/admin/creator-project-detail";
    }

    //

    /*
    admin/project/list
    admin/project/id
    admin/project/approval
    admin/category/list
    admin/user/list
    admin/
     */
}
