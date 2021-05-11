package com.gdufe.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gdufe.yygh.cmn.client.DictFeignClient;
import com.gdufe.yygh.common.result.Result;
import com.gdufe.yygh.enums.DictEnum;
import com.gdufe.yygh.model.user.Patient;
import com.gdufe.yygh.user.mapper.PatientMapper;
import com.gdufe.yygh.user.service.PatientService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

    @Resource
    private DictFeignClient dictFeignClient;

    //根据就诊人id获取就诊人信息
    @Override
    public Patient getPatientById(Long id) {
        return this.packPatient(baseMapper.selectById(id));
    }

    //获取就诊人列表
    @Override
    public List<Patient> findAll(Long userId) {
        //根据id查询就诊人列表
        List<Patient> patients = baseMapper.selectList(new QueryWrapper<Patient>().eq("user_id", userId));
        //通过远程调用,得到编码对应具体内容,查询数据字典表内容
        patients.stream().forEach(item -> {
            this.packPatient(item);
        });
        return patients;
    }

    //查询数据字典表封装Patient
    private Patient packPatient(Patient patient){
        //就诊人证件类型
        String CertificatesName = (String) dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(),patient.getCertificatesType()).getData();
        //联系人证件类型
        String contactsCertificatesTypeString = (String)dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(),patient.getContactsCertificatesType()).getData();
        //省
        String provinceString = (String)dictFeignClient.getName(patient.getProvinceCode()).getData();
        //市
        String cityString = (String)dictFeignClient.getName(patient.getCityCode()).getData();
        //区
        String districtString = (String)dictFeignClient.getName(patient.getDistrictCode()).getData();
        patient.getParam().put("certificatesTypeString", CertificatesName);
        patient.getParam().put("contactsCertificatesTypeString", contactsCertificatesTypeString);
        patient.getParam().put("provinceString", provinceString);
        patient.getParam().put("cityString", cityString);
        patient.getParam().put("districtString", districtString);
        patient.getParam().put("fullAddress", provinceString + cityString + districtString + patient.getAddress());
        return patient;
    }

}
