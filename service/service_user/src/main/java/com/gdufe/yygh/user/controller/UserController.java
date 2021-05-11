package com.gdufe.yygh.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdufe.yygh.common.result.Result;
import com.gdufe.yygh.model.user.UserInfo;
import com.gdufe.yygh.user.service.UserInfoService;
import com.gdufe.yygh.vo.user.UserInfoQueryVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("admin/user")
public class UserController {

    @Resource
    private UserInfoService userInfoService;

    //用户列表(条件查询带分页)
    @GetMapping("{page}/{limit}")
    public Result list(@PathVariable Long page, @PathVariable Long limit, UserInfoQueryVo vo){
        Page<UserInfo> pageParam = new Page<>(page,limit);
        IPage<UserInfo> pageModel = userInfoService.selectPage(pageParam,vo);
        return Result.ok(pageModel);
    }

    //用户锁定
    @GetMapping("lock/{userId}/{status}")
    public Result lock(@PathVariable Long userId,@PathVariable Integer status){
        userInfoService.lock(userId,status);
        return Result.ok();
    }

    //用户详情
    @GetMapping("show/{userId}")
    public Result show(@PathVariable Long userId){
        Map<String,Object> userInfo = userInfoService.show(userId);
        return Result.ok(userInfo);
    }

    //认证审批
    @GetMapping("approval/{userId}/{authStatus}")
    public Result approval(@PathVariable Long userId,@PathVariable Integer authStatus){
        userInfoService.approval(userId,authStatus);
        return Result.ok();
    }

}
