package com.gdufe.yygh.service;

import com.gdufe.yygh.model.hosp.Schedule;
import com.gdufe.yygh.vo.hosp.ScheduleOrderVo;
import com.gdufe.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ScheduleService {
    void save(Map<String, Object> map);

    Page<Schedule> findPage(int page, int limit, ScheduleQueryVo scheduleQueryVo);

    void remove(String hoscode, String hosScheduleId);

    Map<String, Object> getScheduleRule(int page, int limit, String hoscode, String depcode);

    List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate);

    Map<String,Object> getBookingScheduleRule(int page, int limit, String hoscode, String depcode);

    Schedule getScheduleById(String scheduleId);

    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    void update(Schedule schedule);

}
