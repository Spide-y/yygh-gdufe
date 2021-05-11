package com.gdufe.yygh.repository;

import com.gdufe.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository  extends MongoRepository<Hospital,String> {
    //判断是否存在数据
    Hospital getHospitalByHoscode(String hoscode);//方法命名规范，系统可以自动生成对应的方法

    Hospital getHospitalById(String id);

    List<Hospital> findHospitalByHosnameLike(String hosname);
}
