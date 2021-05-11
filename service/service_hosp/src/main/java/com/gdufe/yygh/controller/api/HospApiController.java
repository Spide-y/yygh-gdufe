package com.gdufe.yygh.controller.api;

import com.gdufe.yygh.common.result.Result;
import com.gdufe.yygh.model.hosp.Hospital;
import com.gdufe.yygh.model.hosp.Schedule;
import com.gdufe.yygh.service.DepartmentService;
import com.gdufe.yygh.service.HospitalService;
import com.gdufe.yygh.service.ScheduleService;
import com.gdufe.yygh.vo.hosp.DepartmentVo;
import com.gdufe.yygh.vo.hosp.HospitalQueryVo;
import com.gdufe.yygh.vo.hosp.ScheduleOrderVo;
import com.gdufe.yygh.vo.order.SignInfoVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp/hospital")
public class HospApiController {

    @Resource
    private HospitalService hospitalService;
    @Resource
    private DepartmentService departmentService;
    @Resource
    private ScheduleService scheduleService;

    @ApiOperation("查询医院列表")
    @GetMapping("findHospList/{page}/{limit}")
    public Result findHospitalList(@PathVariable int page,
                                   @PathVariable int limit,
                                   HospitalQueryVo hospitalQueryVo){
        Page<Hospital> hospitals = hospitalService.selectHospPage(page, limit, hospitalQueryVo);
        return Result.ok(hospitals);
    }

    @ApiOperation("根据医院名称模糊查询")
    @GetMapping("findByHospName/{hosname}")
    public Result findByHospName(@PathVariable String hosname){
        List<Hospital> hospitals = hospitalService.findByName(hosname);
        return Result.ok(hospitals);
    }

    @ApiOperation("根据医院编号获取所有的科室信息")
    @GetMapping("findDepartmentByHoscode/{hoscode}")
    public Result findDepartmentByHoscode(@PathVariable String hoscode){
        final List<DepartmentVo> deptList = departmentService.getDeptList(hoscode);
        return Result.ok(deptList);
    }

    @ApiOperation("根据医院编号获取预约挂号详情")
    @GetMapping("findDepDetail/{hoscode}")
    public Result item(@PathVariable String hoscode){
        Map<String,Object> map = hospitalService.item(hoscode);
        return Result.ok(map);
    }

    //获取可预约的排班数据
    @ApiOperation("获取可预约的排班数据")
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getBookingSchedule(@PathVariable int page,@PathVariable int limit,@PathVariable String hoscode,@PathVariable String depcode){
        return Result.ok(scheduleService.getBookingScheduleRule(page,limit,hoscode,depcode));
    }

    //获取排班数据
    @ApiOperation("获取排班数据")
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public Result findScheduleList(@PathVariable String hoscode,@PathVariable String depcode,@PathVariable String workDate){
        return Result.ok(scheduleService.getScheduleDetail(hoscode,depcode,workDate));
    }

    //根据排班id获取排班数据
    @ApiOperation("根据排班id获取排班数据")
    @GetMapping("getSchedule/{scheduleId}")
    public Result getSchedule(@PathVariable String scheduleId){
        Schedule schedule = scheduleService.getScheduleById(scheduleId);
        return Result.ok(schedule);
    }

    //根据排班id获取预约下单数据
    @ApiOperation("根据排班id获取预约下单数据")
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable String scheduleId){
        return scheduleService.getScheduleOrderVo(scheduleId);
    }

    //获取医院签命信息
    @ApiOperation("获取医院签命信息")
    @GetMapping("inner/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(@PathVariable("hoscode") String hoscode){
        return hospitalService.getSignInfoVo(hoscode);
    }

}
