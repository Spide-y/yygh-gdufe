package com.gdufe.yygh.controller.api;

import com.gdufe.yygh.model.hosp.Schedule;
import com.gdufe.yygh.service.ScheduleService;
import com.gdufe.yygh.vo.hosp.ScheduleOrderVo;
import com.gdufe.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;
import com.gdufe.yygh.common.exception.YyghException;
import com.gdufe.yygh.common.helper.HttpRequestHelper;
import com.gdufe.yygh.common.result.Result;
import com.gdufe.yygh.common.result.ResultCodeEnum;
import com.gdufe.yygh.common.utils.MD5;
import com.gdufe.yygh.model.hosp.Department;
import com.gdufe.yygh.model.hosp.Hospital;
import com.gdufe.yygh.service.DepartmentService;
import com.gdufe.yygh.service.HospitalService;
import com.gdufe.yygh.service.HospitalSetService;
import com.gdufe.yygh.vo.hosp.DepartmentQueryVo;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@RestController
@RequestMapping("api/hosp")
public class ApiController {

    @Resource
    private HospitalService hospitalService;
    @Resource
    private HospitalSetService hospitalSetService;
    @Resource
    private DepartmentService departmentService;
    @Resource
    private ScheduleService scheduleService;

    //上传医院接口
    @PostMapping("saveHospital")
    public Result saveHospital(HttpServletRequest req){
        //获取传递过来的信息
        Map<String, String[]> parameterMap = req.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        ///获取医院系统传递过了的签命进行比对，一样才进行添加操作
        String sign = (String) map.get("sign");
        //根据传过来的医院编码，查询数据库，查询签命
        String hoscode = (String)map.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);
        //对签命进行加密
        String signKeyMD5 = MD5.encrypt(signKey);
        //判断
        if (!sign.equals(signKeyMD5)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }else {
            //传输过程中"+"转换为了" "，因此我们需要转换回来
            String logoData = (String) map.get("logoData");
            logoData = logoData.replaceAll(" ","+");
            map.put("logoData",logoData);
            hospitalService.save(map);
            return Result.ok();
        }
    }

    //查询医院接口
    @PostMapping("hospital/show")
    public Result getHospital(HttpServletRequest req){
        //获取传递过来的信息
        Map<String, String[]> parameterMap = req.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        ///获取医院系统传递过了的签命进行比对，一样才进行操作
        String sign = (String) map.get("sign");
        //根据传过来的医院编码，查询数据库，查询签命
        String hoscode = (String)map.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);
        //对签命进行加密
        String signKeyMD5 = MD5.encrypt(signKey);
        //判断
        if (!sign.equals(signKeyMD5)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }else {
            Hospital hospital = hospitalService.getByHoscode(hoscode);
            return Result.ok(hospital);
        }
    }
    //=======================================================================================


    //上传科室
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest req){
        //获取传递过来的科室信息
        Map<String, String[]> parameterMap = req.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);
        ///获取医院系统传递过了的签命进行比对，一样才进行操作
        String sign = (String) map.get("sign");
        //根据传过来的医院编码，查询数据库，查询签命
        String hoscode = (String)map.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);
        //对签命进行加密
        String signKeyMD5 = MD5.encrypt(signKey);
        //判断
        if (!sign.equals(signKeyMD5)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }else {
            departmentService.save(map);
            return Result.ok();
        }
    }

    //查询科室
    @PostMapping("department/list")
    public Result getDepartment(HttpServletRequest req){
        //获取传递过来的科室信息
        Map<String, String[]> parameterMap = req.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        String hoscode = (String)map.get("hoscode");
        //当前页
        int page = StringUtils.isEmpty(map.get("page"))?1:Integer.parseInt((String)map.get("page"));
        int limit = StringUtils.isEmpty(map.get("limit"))?1:Integer.parseInt((String)map.get("limit"));

        ///获取医院系统传递过了的签命进行比对，一样才进行操作
        String sign = (String) map.get("sign");
        //根据传过来的医院编码，查询数据库，查询签命
        String signKey = hospitalSetService.getSignKey(hoscode);
        //对签命进行加密
        String signKeyMD5 = MD5.encrypt(signKey);
        //判断
        if (!sign.equals(signKeyMD5)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }else {
            DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
            departmentQueryVo.setHoscode(hoscode);
            Page<Department> pageModel = departmentService.findPage(page,limit,departmentQueryVo);
            return Result.ok(pageModel);
        }
    }

    //删除科室
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest req){
        //获取传递过来的科室信息
        Map<String, String[]> parameterMap = req.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        //医院编号 和 科室编号
        String hoscode = (String)map.get("hoscode");
        String depcode = (String)map.get("depcode");
        departmentService.remove(hoscode,depcode);
        return Result.ok();
    }

    //======================================================================================================
    //上传排班
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest req){
        //获取传递过来的科室信息
        Map<String, String[]> parameterMap = req.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        //TODO 签命校验
        scheduleService.save(map);

        return Result.ok();
    }

    //查询排班
    @PostMapping("schedule/list")
    public Result getSchedule(HttpServletRequest req){
        //获取传递过来的科室信息
        Map<String, String[]> parameterMap = req.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        String hoscode = (String)map.get("hoscode");
        //当前页
        int page = StringUtils.isEmpty(map.get("page"))?1:Integer.parseInt((String)map.get("page"));
        int limit = StringUtils.isEmpty(map.get("limit"))?1:Integer.parseInt((String)map.get("limit"));

        //TODO 签命校验

        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        Page<Schedule> pageModel = scheduleService.findPage(page,limit,scheduleQueryVo);
        return Result.ok(pageModel);
    }

    //删除排班
    @PostMapping("schedule/remove")
    public Result removeSchedule(HttpServletRequest req){
        //获取传递过来的科室信息
        Map<String, String[]> parameterMap = req.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        //TODO 签命校验

        //医院编号 和 科室编号
        String hoscode = (String)map.get("hoscode");
        String hosScheduleId = (String)map.get("hosScheduleId");
        scheduleService.remove(hoscode,hosScheduleId);
        return Result.ok();
    }

}
