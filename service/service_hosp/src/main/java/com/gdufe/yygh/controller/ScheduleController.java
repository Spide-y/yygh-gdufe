package com.gdufe.yygh.controller;

import com.gdufe.yygh.common.result.Result;
import com.gdufe.yygh.model.hosp.Schedule;
import com.gdufe.yygh.service.ScheduleService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/hosp/schedule")
//@CrossOrigin
public class ScheduleController {

    @Resource
    private ScheduleService scheduleService;

    //根据医院编号和科室编号 查询排班规则数据
    @ApiOperation("根据医院编号和科室编号 查询排班规则数据")
    @GetMapping("getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getScheduleRule(@PathVariable int page,
                                  @PathVariable int limit,
                                  @PathVariable String hoscode,
                                  @PathVariable String depcode){
        Map<String,Object> schedules =  scheduleService.getScheduleRule(page,limit,hoscode,depcode);
        return Result.ok(schedules);
    }

    //根据医院编号、科室编号、工作日期查询排班
    @ApiOperation("查询排班")
    @GetMapping("getSchedule/{hoscode}/{depcode}/{workDate}")
    public Result getSchedule(@PathVariable String hoscode,
                              @PathVariable String depcode,
                              @PathVariable String workDate){
        List<Schedule> list = scheduleService.getScheduleDetail(hoscode,depcode,workDate);
        return Result.ok(list);
    }

}
