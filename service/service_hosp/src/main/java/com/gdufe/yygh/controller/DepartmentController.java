package com.gdufe.yygh.controller;

import com.gdufe.yygh.common.result.Result;
import com.gdufe.yygh.model.hosp.Department;
import com.gdufe.yygh.service.DepartmentService;
import com.gdufe.yygh.vo.hosp.DepartmentVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/admin/hosp/department")
//@CrossOrigin
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;

    //根据医院编号，查询医院的所有科室
    @GetMapping("getDeptList/{hoscode}")
    public Result getDeptList(@PathVariable String hoscode){
        List<DepartmentVo> list = departmentService.getDeptList(hoscode);
        return Result.ok(list);
    }

}
