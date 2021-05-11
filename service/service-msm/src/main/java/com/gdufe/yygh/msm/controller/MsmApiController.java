package com.gdufe.yygh.msm.controller;

import com.gdufe.yygh.common.result.Result;
import com.gdufe.yygh.msm.service.MsmService;
import com.gdufe.yygh.msm.utils.RandomUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/msm")
public class MsmApiController {

    @Resource
    private MsmService msmService;
    @Resource
    private RedisTemplate<String,String> redisTemplate;

    //发送手机验证码
    @GetMapping("send/{phone}")
    public Result sendCode(@PathVariable String phone){
        //从redis中获取验证码
        String code = redisTemplate.opsForValue().get(phone);

        if (!StringUtils.isEmpty(code)){
            return Result.ok();
        }
        //获取不到,生成验证码,通过整合短信服务进行发送
        code = RandomUtil.getSixBitRandom();
        boolean isSend = msmService.send(phone,code);
        //生成验证码放到redis中
        if (isSend){
            redisTemplate.opsForValue().set(phone,code,2, TimeUnit.MINUTES);
            return Result.ok();
        }else {
            return Result.fail("放送短信失败");
        }
    }

}
