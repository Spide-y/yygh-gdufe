package com.gdufe.yygh.user.controller.api;

import com.gdufe.yygh.common.result.Result;
import com.gdufe.yygh.common.utils.AuthContextHolder;
import com.gdufe.yygh.model.user.Patient;
import com.gdufe.yygh.user.service.PatientService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

//就诊人接口
@RestController
@RequestMapping("/api/user/patient")
public class PatientApiController {

    @Resource
    private PatientService patientService;

    //获取就诊人列表
    @ApiOperation("获取就诊人列表")
    @GetMapping("auth/findAll")
    public Result findAll(HttpServletRequest req){
        List<Patient> patientList =  patientService.findAll(AuthContextHolder.getUserId(req));
        return Result.ok(patientList);
    }

    //添加就诊人
    @PostMapping("auth/save")
    public Result savePatient(@RequestBody Patient patient, HttpServletRequest req) {
        Long userId = AuthContextHolder.getUserId(req);
        patient.setUserId(userId);
        patientService.save(patient);
        return Result.ok();
    }

    //根据就诊人id获取就诊人信息
    @GetMapping("auth/get/{id}")
    public Result getPatient(@PathVariable Long id){
        Patient patient = patientService.getPatientById(id);
        return Result.ok(patient);
    }

    //修改就诊人
    @PostMapping("auth/update")
    public Result updatePatient(@RequestBody Patient patient) {
        patientService.updateById(patient);
        return Result.ok();
    }

    //删除就诊人
    @DeleteMapping("auth/delete/{id}")
    public Result deletePatient(@PathVariable Long id){
        patientService.removeById(id);
        return Result.ok();
    }

    //根据就诊人id获取就诊人信息
    @GetMapping("inner/get/{id}")
    public Patient getPatientOrder(@PathVariable Long id){
        Patient patient = patientService.getPatientById(id);
        return patient;
    }

}
