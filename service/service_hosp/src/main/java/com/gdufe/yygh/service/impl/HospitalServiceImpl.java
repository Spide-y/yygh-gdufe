package com.gdufe.yygh.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gdufe.yygh.cmn.client.DictFeignClient;
import com.gdufe.yygh.common.exception.YyghException;
import com.gdufe.yygh.common.result.Result;
import com.gdufe.yygh.common.result.ResultCodeEnum;
import com.gdufe.yygh.mapper.HospitalSetMapper;
import com.gdufe.yygh.model.hosp.Hospital;
import com.gdufe.yygh.model.hosp.HospitalSet;
import com.gdufe.yygh.repository.HospitalRepository;
import com.gdufe.yygh.service.HospitalService;
import com.gdufe.yygh.vo.hosp.HospitalQueryVo;
import com.gdufe.yygh.vo.order.SignInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class HospitalServiceImpl extends ServiceImpl<HospitalSetMapper,HospitalSet> implements HospitalService {

    @Resource
    private HospitalRepository repository;
    @Resource
    private DictFeignClient dictFeignClient;

    //获取医院签命信息
    @Override
    public SignInfoVo getSignInfoVo(String hoscode) {
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        wrapper.eq("hoscode",hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        if (null == hospitalSet){
            throw new YyghException(ResultCodeEnum.HOSPITAL_OPEN);
        }
        SignInfoVo signInfoVo = new SignInfoVo();
        signInfoVo.setApiUrl(hospitalSet.getApiUrl());
        signInfoVo.setSignKey(hospitalSet.getSignKey());

        return signInfoVo;
    }

    //根据医院编号获取预约挂号详情
    @Override
    public Map<String, Object> item(String hoscode) {
        Map<String, Object> result = new HashMap<>();
        //医院详情
        Hospital hospital = this.setHospitalHosType(this.getByHoscode(hoscode));
        result.put("hospital",hospital);
        //预约规则
        result.put("bookingRule",hospital.getBookingRule());
        //不需要重复返回
        hospital.setBookingRule(null);
        return result;
    }

    //根据名字模糊查询
    @Override
    public List<Hospital> findByName(String hosname) {
        return repository.findHospitalByHosnameLike(hosname);
    }

    @Override
    public String getHospName(String hoscode) {
        Hospital hospital = repository.getHospitalByHoscode(hoscode);
        return hospital.getHosname();
    }

    @Override
    public void save(Map<String, Object> map) {
        //把集合转换成对象(Hospital)
        String mapString = JSONObject.toJSONString(map);//先转换成字符串
        Hospital hospital = JSONObject.parseObject(mapString, Hospital.class);

        //先判断是否存在相同的数据
        String hoscode = hospital.getHoscode();
        Hospital hExist = repository.getHospitalByHoscode(hoscode);

        ////如果存在，更新;如果不存在，添加
        if (hExist!=null){
            hospital.setStatus(hExist.getStatus());
            hospital.setCreateTime(hExist.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            repository.save(hospital);
        }else {
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            repository.save(hospital);
        }
    }

    @Override
    public Hospital getByHoscode(String hoscode) {
        Hospital hospital = repository.getHospitalByHoscode(hoscode);
        return hospital;
    }

    //通过查询mongodb得到医院列表
    @Override
    public Page selectHospPage(int page, int limit, HospitalQueryVo vo) {

        //创建Pageable对象
        Pageable pageable = PageRequest.of(page-1,limit);
        //构建条件匹配器
        ExampleMatcher matcher = ExampleMatcher.matching() //ExampleMatcher.matching()给定查找条件
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //ExampleMatcher.StringMatcher.CONTAINING:代表模糊查询
                .withIgnoreCase(true);//忽略大小写

        //vo转换成Hospital
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(vo,hospital);
        //创建Example对象
        Example<Hospital> example = Example.of(hospital,matcher);
        //调用方法实现
        Page<Hospital> pageHosp = repository.findAll(example, pageable);
        //得到医院集合,医院对象中有个Map属性可以用来存医院等级信息
        List<Hospital> pages = pageHosp.getContent();
        //循环为每个对象加速医院等级 等属性
//        for (Hospital hosp:pages){
//            this.setHospitalHosType(hosp);
//        }
        pages.stream().forEach(hosp -> {
            hosp = this.setHospitalHosType(hosp);
        });

        //dictFeignClient.getName();

        return pageHosp;
    }

    //获取查询list集合,遍历进行医院等级,省,市,地区封装
    private Hospital setHospitalHosType(Hospital hosp){
        Result resultType = dictFeignClient.getName("Hostype", hosp.getHostype());
        Result resultProvince = dictFeignClient.getName(hosp.getProvinceCode());
        Result resultCity = dictFeignClient.getName(hosp.getCityCode());
        Result resultDistrict = dictFeignClient.getName(hosp.getDistrictCode());
        String type = (String) resultType.getData();
        String province = (String)resultProvince.getData();
        String city = (String)resultCity.getData();
        String district = (String)resultDistrict.getData();
        hosp.getParam().put("type",type);
        hosp.getParam().put("fullAddress",province+city+district);
        return hosp;
    }

    //更新医院上线状态
    @Override
    public void updateStatus(String id,Integer status) {
        Hospital hospital = repository.findById(id).get();
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        repository.save(hospital);
    }

    //获取医院详情
    @Override
    public Map<String,Object> getInfo(String id) {
        Map<String,Object> result = new HashMap<>();
        Hospital hospital = this.setHospitalHosType(repository.getHospitalById(id));
        result.put("hospital",hospital);
        result.put("bookRule",hospital.getBookingRule());
        return result;
    }

}
