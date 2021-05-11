package com.gdufe.yygh.msm.service;

import com.gdufe.yygh.vo.msm.MsmVo;

public interface MsmService {
    boolean send(String phone, String code);

    //mq使用发送短信
    boolean send(MsmVo msmVo);
}
