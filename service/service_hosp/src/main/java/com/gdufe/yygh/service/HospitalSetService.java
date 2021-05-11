package com.gdufe.yygh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gdufe.yygh.model.hosp.HospitalSet;

public interface HospitalSetService extends IService<HospitalSet> {
    String getSignKey(String hoscodes);
}
