package com.dkthanh.demo.controller;

import com.dkthanh.demo.domain.CategoryEntity;
import com.dkthanh.demo.domain.NewUserDTO;
import com.dkthanh.demo.domain.UserDetailEntity;
import com.dkthanh.demo.domain.UserEntity;
import com.dkthanh.demo.domain.dto.ProjectFullInfoEntity;
import com.dkthanh.demo.dto.ProjectDto;
import com.dkthanh.demo.service.CategoryService;
import com.dkthanh.demo.service.ProjectService;
import com.dkthanh.demo.service.UserDetailService;
import com.dkthanh.demo.service.UserService;
import com.dkthanh.demo.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserDetailService userDetailService;

    /*
     *  Common function
     * ===========================================
     */
    private String doUpload(HttpServletRequest request, Model model, //
                            ProjectDto myUploadForm) {

        String description = myUploadForm.getImageName();
        System.out.println("Description: " + description);

        // Thư mục gốc upload file.
        String uploadRootPath = request.getServletContext().getRealPath("upload");
        System.out.println("uploadRootPath=" + uploadRootPath);

        File uploadRootDir = new File(uploadRootPath);
        // Tạo thư mục gốc upload nếu nó không tồn tại.
        if (!uploadRootDir.exists()) {
            uploadRootDir.mkdirs();
        }
        MultipartFile fileDatas = myUploadForm.getFileDatas();
        // Tên file gốc tại Client.
        String name = fileDatas.getOriginalFilename();
        System.out.println("Client File Name = " + name);

        if (name != null && name.length() > 0) {
            try {
                // Tạo file tại Server.
                File serverFile = new File(uploadRootDir.getAbsolutePath() + File.separator + name);

                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
                stream.write(fileDatas.getBytes());
                stream.close();
                //
//                    uploadedFiles.add(serverFile);
                System.out.println("Write file: " + serverFile);
            } catch (Exception e) {
                System.out.println("Error Write file: " + name);
//                    failedFiles.add(name);
            }
        }

        return  uploadRootDir.getAbsolutePath() + File.separator + name;
    }

    // load thumbnail image
    @RequestMapping(value = { "/project/image/{project_id}" }, method = RequestMethod.GET)
    public void productImage(HttpServletRequest request, HttpServletResponse response, Model model,
                             @PathVariable("project_id") int projectId) throws IOException {
        String path = projectService.getProjectDetail(projectId).getMaterialThumbnailPath();
        response.setContentType("image/jpeg, image/jpg, image/png, image/gif");
        response.getOutputStream().write(WebUtil.extractByte(path));
        response.getOutputStream().close();
    }

    // return temp result for testing
    @GetMapping(value = "/temp-result")
    public String tempResult(){
        return "/creator/test-upload";
    }

    //    Open register page
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String openRegisterForm(Model model){
        NewUserDTO newUserDTO = new NewUserDTO();
        model.addAttribute("newUserForm", newUserDTO);
        return "register-page";
    }

    //    Save new user
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String registerNewAccount(Model model, @ModelAttribute("newUserForm") @Validated NewUserDTO newUserDTO, BindingResult result, final RedirectAttributes redirectAttributes){
        // Validate result
        if (result.hasErrors()) {
            return "404";
        }

        UserEntity newUser = new UserEntity();
        try{
            newUser = userService.saveUser(newUserDTO);
        }catch (Exception e){
            return "register";
        }
        return "temp-result";
    }

    // open login form
    @GetMapping(value = "/signin")
    public String login() {
        return "/login-page";
    }

    // search function - not complete
    @PostMapping(value = "/search", produces = { "application/json" })
    public String search(@RequestParam("search_box") String keyword, Model model){
        if (StringUtils.isEmpty(keyword)) {
            model.addAttribute("result", 0);
            model.addAttribute("infor", new String("We can't find any result with your keyword"));
            return "client/search_result";
        }
        model.addAttribute("result", 1);
        model.addAttribute("search_results", projectService.searchProjectByNameContaining(keyword));
        model.addAttribute("tags", categoryService.getAllCategory());
        return "/search_result";
    }
    /*
     *  index page function
     * ===========================================
     */

    //    index page
    @GetMapping(value = "/index")
    public String index(Model model){
        List<CategoryEntity> categoryEntityList = categoryService.getAllCategory();
        List<ProjectFullInfoEntity> popularProjects = projectService.getPopularProjects();
        ProjectFullInfoEntity recommendedProject = projectService.getRecommendedProject();
        model.addAttribute("categories", categoryEntityList);
        model.addAttribute("popular_projects", popularProjects);
        model.addAttribute("recommended_project", recommendedProject);
        return "index";
    }

    @GetMapping(value = "/")
    public String getHomePage(){
        return "redirect:/index";
    }



    /*
     *  Project detail page function
     * ===========================================
     */

    //  load project detail page
    @GetMapping(value = "/project/{id}")
    public String getProjectDetailPage(Model model, @PathVariable("id") Integer id){
        ProjectFullInfoEntity p = projectService.getProjectDetail(id);

        model.addAttribute("project", p);

        return "project-detail";
    }
    /*
     *  All project page function
     * ===========================================
     */


    /*
     *  Sys management function
     * ===========================================
     * creator/project/list
     * creator/create-project
     * creator/project/id
     * creator/project/save
     * creator/project/edit/id
     * creator/project/delete/id
     */

    // creator project/list
    @GetMapping(value = "/creator/project/list" )
    public String getCreatorProjectList(Model model, Authentication authentication){
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

        List<ProjectFullInfoEntity> list = projectService.getProjectListOfCreator(userId);
        model.addAttribute("project-list", list);
        model.addAttribute("creator", userDetailEntity);
        return "/creator/creator-dashboard";
    }

    // creator project/id
    @GetMapping(value = "/creator/project/{id}")
    public String getProjectDetail(Model model, @PathVariable("id") Integer projectId, HttpServletRequest request, Authentication authentication){
        ProjectFullInfoEntity p = projectService.getProjectDetail(projectId);
        model.addAttribute("project", p);
        return "/creator/creator-project-detail";
    }

    //create project
    @GetMapping(value = "/creator/create-project")
    public String openCreateProjectForm(Model model){
        ProjectDto dto = new ProjectDto();
        model.addAttribute("allCategory", categoryService.getAllCategory());
        model.addAttribute("project_dto", dto);
        return "/creator/create-project";
    }

    @PostMapping(value = "/creator/save-project")
    public String saveProject(HttpServletRequest request,Model model, @ModelAttribute("project_dto") @Validated ProjectDto dto,
                              BindingResult result, final RedirectAttributes redirectAttributes){
        if (result.hasErrors()) {
            return "redirect:/index";
        }


        return "redirect:/creator/create-project";
    }


    /*
    *  Admin management function
    * ===========================================
    admin/project/list
    admin/project/id
    admin/project/approval
    admin/category/list
    admin/user/list
    admin/
     */

    // get project list
    @GetMapping(value = "/admin/project/list")
    public String getAdminProjectList(){
        return null;
    }

    // get pending list of project that need approval
    @GetMapping(value = "/admin/project/pending-list")
    public String getAdminPendingList(){
        return null;
    }

    // category list for admin view
    @GetMapping(value = "/admin/category/list")
    public String getCategoryList(){
        return null;
    }

    // get admin dashboard
    @GetMapping(value = "/admin/dashboard")
    public String getAdminDashboard(){
        return null;
    }
}
