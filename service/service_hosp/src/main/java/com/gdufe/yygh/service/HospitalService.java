package com.gdufe.yygh.service;

import com.gdufe.yygh.model.hosp.Hospital;
import com.gdufe.yygh.vo.hosp.HospitalQueryVo;
import com.gdufe.yygh.vo.order.SignInfoVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface HospitalService {
    void save(Map<String, Object> map);

    Hospital getByHoscode(String hoscode);

    Page selectHospPage(int page, int limit, HospitalQueryVo vo);

    void updateStatus(String id,Integer status);

    Map<String,Object> getInfo(String id);

    String getHospName(String hoscode);

    List<Hospital> findByName(String hosname);

    Map<String, Object> item(String hoscode);

    SignInfoVo getSignInfoVo(String hoscode);
}
