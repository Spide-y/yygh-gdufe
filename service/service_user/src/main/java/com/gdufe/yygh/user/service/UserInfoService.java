package com.gdufe.yygh.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdufe.yygh.model.user.UserInfo;
import com.gdufe.yygh.vo.user.LoginVo;
import com.gdufe.yygh.vo.user.UserAuthVo;
import com.gdufe.yygh.vo.user.UserInfoQueryVo;

import java.util.Map;

public interface UserInfoService {
    Map<String, Object> loginUser(LoginVo loginVo);

    boolean save(UserInfo user);

    UserInfo selectWxInfoOpenId(String openid);

    void userAuth(Long userId, UserAuthVo userAuthVo);

    UserInfo getById(Long userId);

    IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo vo);

    void lock(Long userId, Integer status);

    Map<String, Object> show(Long userId);

    void approval(Long userId, Integer authStatus);
}
