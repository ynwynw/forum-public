package com.milanuo.springboot2mybatisforum.web.admin.admin_user;

import com.milanuo.springboot2mybatisforum.core.CommomMethod.MD5Util;
import com.milanuo.springboot2mybatisforum.core.PageResult.BasePageResult;
import com.milanuo.springboot2mybatisforum.core.Query4Object.Query4Topics;
import com.milanuo.springboot2mybatisforum.core.ajax.AjaxResult;
import com.milanuo.springboot2mybatisforum.module.user.pojo.User;
import com.milanuo.springboot2mybatisforum.module.web.pojo.SysRole;
import com.milanuo.springboot2mybatisforum.module.web.pojo.UserInfo;
import com.milanuo.springboot2mybatisforum.module.web.pojo.UserRole;
import com.milanuo.springboot2mybatisforum.module.web.service.SysRoleService;
import com.milanuo.springboot2mybatisforum.module.web.service.UserInfoService;
import com.milanuo.springboot2mybatisforum.module.web.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/admin/admin_user")
public class AdminUserController {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private UserRoleService userRoleService;


    @GetMapping("/list")
    public String list(Integer pageNo, Model model) {
        Query4Topics query4Topics = new Query4Topics();
        if (pageNo != null) {
            query4Topics.setPageNum(pageNo);
        } else {
            query4Topics.setPageNum(1);
        }
        query4Topics.setPageSize(20);
        List<UserInfo> userInfoList = userInfoService.getAllUserInfo(query4Topics);
        BasePageResult basePageResult = new BasePageResult();
        basePageResult.setPageNum(query4Topics.getPageNum());
        basePageResult.setPageSize(query4Topics.getPageSize());
        basePageResult.setTotalCount(userInfoService.getAllUserInfoCount(query4Topics));
        model.addAttribute("userInfoList", userInfoList);
        model.addAttribute("basePageResult", basePageResult);

        return "admin/admin_user/list";
    }

    @GetMapping("/block")
    @ResponseBody
    public AjaxResult block(Integer id) {

        AjaxResult ajaxResult = new AjaxResult();
        UserInfo userInfo = new UserInfo();

        try {
            userInfo.setId(id);
            userInfo.setState((byte) 2);
            userInfoService.update(userInfo);
            ajaxResult.setSuccessful(true);
            ajaxResult.setDescribe("????????????");
        } catch (Exception e) {
            e.printStackTrace();
            ajaxResult.setDescribe("????????????????????????");
            ajaxResult.setSuccessful(false);
        }

        return ajaxResult;
    }

    @GetMapping("/usering")
    @ResponseBody
    public AjaxResult usering(Integer id) {

        AjaxResult ajaxResult = new AjaxResult();
        UserInfo userInfo = new UserInfo();

        try {
            userInfo.setId(id);
            userInfo.setState((byte) 1);
            userInfoService.update(userInfo);
            ajaxResult.setSuccessful(true);
            ajaxResult.setDescribe("????????????");
        } catch (Exception e) {
            e.printStackTrace();
            ajaxResult.setDescribe("????????????????????????");
            ajaxResult.setSuccessful(false);
        }

        return ajaxResult;
    }

    @GetMapping("/delete")
    @ResponseBody
    public AjaxResult delete(Integer id) {

        AjaxResult ajaxResult = new AjaxResult();

        try {
            //??????userInfo
            userInfoService.delete(id);

            ajaxResult.setSuccessful(true);
            ajaxResult.setDescribe("????????????");
        } catch (Exception e) {
            e.printStackTrace();
            ajaxResult.setDescribe("????????????????????????");
            ajaxResult.setSuccessful(false);
        }

        return ajaxResult;
    }

    @GetMapping("/editPage")
    public String editPage(Integer id, Model model) {
        //????????????ID?????????
        UserInfo userInfo = userInfoService.getUserInfoById(id);
        //?????????????????????
        List<SysRole> sysRoleList = sysRoleService.getAllRole();
        //????????????????????????????????????ID
        List<Integer> userRolesIds = userRoleService.getRolesIdByUserId(id);

        model.addAttribute("userInfo", userInfo);
        model.addAttribute("sysRoleList", sysRoleList);
        model.addAttribute("userRolesIds", userRolesIds);

        return "admin/admin_user/edit";
    }

    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult edit(Integer id, String username, String oldPassword, String password, String roleId) {

        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.setSuccessful(true);
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        //???????????????oldPassword,??????oldPassword????????????,????????????????????????
        if (oldPassword != null && !"".equals(oldPassword.trim())) {
            String ps = userInfoService.findByUsername(username).getPassword();
            String pso = MD5Util.getMD5Password(oldPassword, username).toString();
            if (ps.equals(pso)) {
                if (password != null && !"".equals(password.trim())) {
                    userInfo.setPassword(MD5Util.getMD5Password(password,username).toString());
                    userInfoService.update(userInfo);
                } else {
                    ajaxResult.setDescribe("??????????????????????????????");
                    ajaxResult.setSuccessful(false);
                }

            } else {
                ajaxResult.setDescribe("??????????????????????????????????????????");
                ajaxResult.setSuccessful(false);
            }
        }

        if (ajaxResult.getSuccessful()) {
            try {
                //??????userInfo_sysRole?????????userInfo???????????????
                userRoleService.deleteByUserInfoId(id);

                //????????????????????????????????????userInfo???
                if (roleId != null && !"".equals(roleId)) {
                    String[] roleIds = roleId.split(",");
                    for (String stringId : roleIds) {
                        Integer rid = Integer.valueOf(stringId);
                        UserRole userRole = new UserRole();
                        userRole.setUserId(id);
                        userRole.setRoleId(rid);
                        userRoleService.save(userRole);
                    }
                }

                ajaxResult.setSuccessful(true);
                ajaxResult.setDescribe("??????????????????");

            } catch (Exception e) {
                e.printStackTrace();
                ajaxResult.setDescribe("????????????????????????");
                ajaxResult.setSuccessful(false);
            }

        }

        return ajaxResult;

    }

    @GetMapping("/addPage")
    public String addPage(Model model) {

        //?????????????????????
        List<SysRole> sysRoleList = sysRoleService.getAllRole();

        model.addAttribute("sysRoleList", sysRoleList);

        return "admin/admin_user/add";
    }

    @PostMapping("/add")
    @ResponseBody
    public AjaxResult add(String username, String password, String roleId) {

        AjaxResult ajaxResult = new AjaxResult();
        UserInfo userInfo = new UserInfo();
        Object password1 = MD5Util.getMD5Password(password, username);
        try {
            userInfo.setUsername(username);
            userInfo.setPassword(password1.toString());
            userInfo.setState((byte) 1);
            userInfo.setInTime(new Date());
            userInfoService.save(userInfo);

            if (userInfo.getId() != null) {
                //????????????????????????????????????userInfo???
                if (roleId != null && !"".equals(roleId)) {
                    String[] roleIds = roleId.split(",");
                    for (String stringId : roleIds) {
                        Integer rid = Integer.valueOf(stringId);
                        UserRole userRole = new UserRole();
                        userRole.setUserId(userInfo.getId());
                        userRole.setRoleId(rid);
                        userRoleService.save(userRole);
                    }
                }
            }
            ajaxResult.setDescribe("??????????????????");
            ajaxResult.setSuccessful(true);
        } catch (Exception e) {
            e.printStackTrace();
            ajaxResult.setDescribe("?????????????????????");
            ajaxResult.setSuccessful(false);
        }

        return ajaxResult;
    }


}
