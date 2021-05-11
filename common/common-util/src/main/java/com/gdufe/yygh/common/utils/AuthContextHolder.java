package com.gdufe.yygh.common.utils;

import com.gdufe.yygh.common.helper.JwtHelper;

import javax.servlet.http.HttpServletRequest;

//获取当前用户信息工具类
public class AuthContextHolder {

    //获取当前用户id
    public static Long getUserId(HttpServletRequest req){
        String token = req.getHeader("token");
        Long userId = JwtHelper.getUserId(token);
        return userId;
    }

    //获取当前用户名称
    public static String getUserName(HttpServletRequest req){
        String token = req.getHeader("token");
        String userName = JwtHelper.getUserName(token);
        return userName;
    }

}
