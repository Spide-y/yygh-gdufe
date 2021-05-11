package com.gdufe.yygh.service;

import com.gdufe.yygh.model.hosp.Department;
import com.gdufe.yygh.vo.hosp.DepartmentQueryVo;
import com.gdufe.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    void save(Map<String, Object> map);

    Page<Department> findPage(int page, int limit, DepartmentQueryVo departmentQueryVo);

    void remove(String hoscode, String depcode);

    List<DepartmentVo> getDeptList(String hoscode);

    public Department getDepartment(String hoscode,String depcode);

    String getDepName(String hoscode,String depcode);
}
