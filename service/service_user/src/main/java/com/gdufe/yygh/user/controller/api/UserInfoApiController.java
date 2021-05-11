package com.gdufe.yygh.user.controller.api;

import com.gdufe.yygh.common.result.Result;
import com.gdufe.yygh.common.utils.AuthContextHolder;
import com.gdufe.yygh.model.user.UserInfo;
import com.gdufe.yygh.user.service.UserInfoService;
import com.gdufe.yygh.vo.user.LoginVo;
import com.gdufe.yygh.vo.user.UserAuthVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserInfoApiController {

    @Resource
    private UserInfoService userInfoService;

    //用户手机号登录
    @ApiOperation("会员登录(手机登录)")
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo){
        Map<String,Object> map = userInfoService.loginUser(loginVo);
        return Result.ok(map);
    }

    //用户认证接口
    @PostMapping("auth/userAuth")
    public Result userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest req){
        //传递两个参数 第一个参数:用户id 第二个参数:vo对象
        userInfoService.userAuth(AuthContextHolder.getUserId(req),userAuthVo);
        return Result.ok();
    }

    //获取用户id信息接口
    @GetMapping("auth/getUserInfo")
    public Result getUserInfo(HttpServletRequest req){
        Long userId = AuthContextHolder.getUserId(req);
        UserInfo userInfo = userInfoService.getById(userId);
        return Result.ok(userInfo);
    }

}
