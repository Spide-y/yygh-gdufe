package com.gdufe.yygh.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.gdufe.yygh.vo.hosp.DepartmentVo;
import lombok.val;
import org.springframework.data.domain.Page;
import com.gdufe.yygh.model.hosp.Department;
import com.gdufe.yygh.repository.DepartmentRepository;
import com.gdufe.yygh.service.DepartmentService;
import com.gdufe.yygh.vo.hosp.DepartmentQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Resource
    private DepartmentRepository repository;

    public Department getDepartment(String hoscode,String depcode){
        return repository.getDepartmentByHoscodeAndDepcode(hoscode,depcode);
    }

    @Override
    public String getDepName(String hoscode,String depcode) {
        Department dep = repository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        return dep.getDepname();
    }

    @Override
    public void remove(String hoscode, String depcode) {
        //根据医院编号 和 科室编号查询科室信息
        Department department = repository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);

        if (department!=null){
            repository.deleteById(department.getId());
        }
    }

    @Override
    public Page<Department> findPage(int page, int limit, DepartmentQueryVo departmentQueryVo) {
        //创建Pageable对象 设置当前页和每页记录数
        Pageable pageable = PageRequest.of(page-1,limit);
        //创建Example对象
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo,department);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example<Department> example = Example.of(department,matcher);

        Page<Department> departments = repository.findAll(example, pageable);
        return departments;
    }

    @Override
    public void save(Map<String, Object> map) {
        //转换成Department对象
        String mapString = JSONObject.toJSONString(map);
        Department department = JSONObject.parseObject(mapString,Department.class);

        Department depExist = repository.getDepartmentByHoscodeAndDepcode(department.getHoscode(),department.getDepcode());

        if (depExist!=null){
            depExist.setUpdateTime(new Date());
            depExist.setIsDeleted(0);
            repository.save(depExist);
        }else {
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            repository.save(department);
        }
    }

    //获取列表
    @Override
    public List<DepartmentVo> getDeptList(String hoscode) {
        //建造List用于封装
        List<DepartmentVo> result = new ArrayList<>();

        //查询所有科室信息
        Department query = new Department();
        query.setHoscode(hoscode);
        Example<Department> example = Example.of(query);//构造一个Example
        List<Department> departments = repository.findAll(example);

        //封装成大科室 小科室格式的json数据
        //根据大科室编号
        Map<String,List<Department>> depMap = departments.stream().collect(Collectors.groupingBy(Department::getBigcode));
        //遍历map集合
        for (Map.Entry<String,List<Department>> entry : depMap.entrySet()){
            //大科室编号
            String bigCode = entry.getKey();
            //大科室编号对应的数据
            List<Department> depList = entry.getValue();
            //封装大科室
            DepartmentVo departmentVo1 = new DepartmentVo();
            departmentVo1.setDepcode(bigCode);
            departmentVo1.setDepname(depList.get(0).getBigname());
            //封装小科室
            List<DepartmentVo> children = new ArrayList<>();
            for (Department department:depList){
                DepartmentVo departmentVo2 = new DepartmentVo();
                departmentVo2.setDepcode(department.getDepcode());
                departmentVo2.setDepname(department.getDepname());
                //封装到List中
                children.add(departmentVo2);
            }
            //把小科室list封装到大科室children里面
            departmentVo1.setChildren(children);
            //放到result中
            result.add(departmentVo1);

        }

        return result;
    }


}
