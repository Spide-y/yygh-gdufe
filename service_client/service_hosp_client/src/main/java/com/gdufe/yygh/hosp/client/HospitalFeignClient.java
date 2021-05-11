package com.gdufe.yygh.hosp.client;

import com.gdufe.yygh.vo.hosp.ScheduleOrderVo;
import com.gdufe.yygh.vo.order.SignInfoVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-hosp")
@Repository
public interface HospitalFeignClient {

    //根据排班id获取预约下单数据
    @ApiOperation("根据排班id获取预约下单数据")
    @GetMapping("/api/hosp/hospital/inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId);

    //获取医院签命信息
    @ApiOperation("获取医院签命信息")
    @GetMapping("/api/hosp/hospital/inner/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(@PathVariable("hoscode") String hoscode);

}
