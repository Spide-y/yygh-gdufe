package com.gdufe.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gdufe.yygh.common.exception.YyghException;
import com.gdufe.yygh.common.helper.JwtHelper;
import com.gdufe.yygh.common.result.ResultCodeEnum;
import com.gdufe.yygh.enums.AuthStatusEnum;
import com.gdufe.yygh.model.user.Patient;
import com.gdufe.yygh.model.user.UserInfo;
import com.gdufe.yygh.user.mapper.UserInfoMapper;
import com.gdufe.yygh.user.service.PatientService;
import com.gdufe.yygh.user.service.UserInfoService;
import com.gdufe.yygh.vo.user.LoginVo;
import com.gdufe.yygh.vo.user.UserAuthVo;
import com.gdufe.yygh.vo.user.UserInfoQueryVo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper,UserInfo> implements UserInfoService {

    @Resource
    private RedisTemplate<String,String> redisTemplate;
    @Resource
    private PatientService patientService;

    //认证审批
    @Override
    public void approval(Long userId, Integer authStatus) {
        if (authStatus.intValue()==2||authStatus.intValue()==-1){
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }
    }

    //用户详情
    @Override
    public Map<String, Object> show(Long userId) {
        Map<String,Object> result = new HashMap<>();
        //根据id查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);
        this.packUserInfo(userInfo);
        result.put("userInfo",userInfo);

        //根据id查就诊人信息
        List<Patient> patientList = patientService.findAll(userId);
        result.put("patientList",patientList);

        return result;
    }

    //用户锁定
    @Override
    public void lock(Long userId, Integer status) {
        if (status.intValue() == 0 || status.intValue() == 1){
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setStatus(status);
            baseMapper.updateById(userInfo);
        }
    }

    //用户列表(条件查询带分页)
    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo vo) {
        //vo获取条件值
        String name = vo.getKeyword();//用户名称
        Integer status = vo.getStatus();//用户状态
        Integer authStatus = vo.getAuthStatus();//认证状态
        String createTimeBegin = vo.getCreateTimeBegin();//开始时间
        String createTimeEnd = vo.getCreateTimeEnd();//结束时间
        //对条件进行判断
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(name)){
            wrapper.like("name",name);
        }
        if (!StringUtils.isEmpty(status)){
            wrapper.eq("status",status);
        }
        if (!StringUtils.isEmpty(authStatus)){
            wrapper.eq("auth_status",authStatus);
        }
        if (!StringUtils.isEmpty(createTimeBegin)){
            wrapper.ge("create_timeBegin",createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)){
            wrapper.le("create_timeEnd",createTimeEnd);
        }
        //调用mapper
        Page<UserInfo> pages = baseMapper.selectPage(pageParam, wrapper);
        //编号变成对应的值
        pages.getRecords().stream().forEach(item->{
            this.packUserInfo(item);
        });

        return pages;
    }

    @Override
    public UserInfo getById(Long userId) {
        return baseMapper.selectById(userId);
    }

    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //根据用户id查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);

        //设置认证信息
        //认证人姓名
        userInfo.setName(userAuthVo.getName());
        //其他认证信息
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());

        //进行信息更新
        baseMapper.updateById(userInfo);
    }

    @Override
    public UserInfo selectWxInfoOpenId(String openid) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("openid",openid);
        UserInfo user = baseMapper.selectOne(queryWrapper);
        return user;
    }

    @Override
    public boolean save(UserInfo entity) {
        if (baseMapper.insert(entity) != 0)
        return true;
        else return false;
    }

    @Override
    public Map<String, Object> loginUser(LoginVo loginVo) {
        //从Vo对象中获取输入的手机号和验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();

        //判断是否为null
        if (StringUtils.isEmpty(phone)||StringUtils.isEmpty(code)){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        //判断验证码是否一致
        String redisCode = redisTemplate.opsForValue().get(phone);
        System.out.println("====="+redisCode+"********"+code+"=====");
        if (!code.equals(redisCode)){
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }

        //绑定手机号
        UserInfo info = null;
        if (!StringUtils.isEmpty(loginVo.getOpenid())){
            info = this.selectWxInfoOpenId(loginVo.getOpenid());
            if (null != info){
                info.setPhone(loginVo.getPhone());
                this.updateById(info);
            }else {
                throw new YyghException(ResultCodeEnum.DATA_ERROR);
            }
        }

        //如果userInfo为空
        if (info == null) {
            //判断是否第一次登录:根据手机号查数据库(不是第一次，直接登录)
            info = baseMapper.selectOne(new QueryWrapper<UserInfo>().eq("phone", phone));

            if (info == null){
                //如果为空 添加信息到数据库
                info = new UserInfo();
                info.setName(null);
                info.setPhone(phone);
                info.setStatus(1);
                baseMapper.insert(info);
            }
        }


        if (info.getStatus() == 0){
            throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }

        //返回登录信息
        Map<String,Object> result = new HashMap<>();
        String name = info.getName();
        if (StringUtils.isEmpty(name)){
            name = info.getNickName();
        }
        if (StringUtils.isEmpty(name)){
            name = info.getPhone();
        }
        result.put("name",name);
        //返回token信息
        //JWT 生成token
        String token = JwtHelper.createToken(info.getId(), name);
        result.put("token",token);

        return result;
    }

    //封装UserInfo
    private UserInfo packUserInfo(UserInfo userInfo){
        //认证状态
        userInfo.getParam().put("authStatusString",AuthStatusEnum.getStatusNameByStatus(userInfo.getStatus()));
        //用户状态
        String statusString = userInfo.getStatus().intValue()==0?"锁定":"正常";
        userInfo.getParam().put("statusString",statusString);
        return userInfo;
    }

}
