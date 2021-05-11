package com.gdufe.yygh.controller;

import com.gdufe.yygh.common.result.Result;
import com.gdufe.yygh.model.hosp.Hospital;
import com.gdufe.yygh.service.HospitalService;
import com.gdufe.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/admin/hosp/hospital")
//@CrossOrigin
public class HospitalController {

    @Resource
    private HospitalService hospitalService;

    //医院列表(条件查询分页)
    @ApiOperation("医院列表(条件查询分页)")
    @GetMapping("list/{page}/{limit}")
    public Result listHospital(@PathVariable int page, @PathVariable int limit, HospitalQueryVo vo){
        Page pageModel = hospitalService.selectHospPage(page,limit,vo);
        return Result.ok(pageModel);
    }

    //更新医院上线状态
    @GetMapping("updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable String id,@PathVariable Integer status){
        hospitalService.updateStatus(id,status);
        return Result.ok();
    }

    //医院详情信息
    @GetMapping("getInfo/{id}")
    public Result getInfo(@PathVariable String id){
        Map<String,Object> hospitalMap = hospitalService.getInfo(id);
        return Result.ok(hospitalMap);
    }

}
