package com.gdufe.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.gdufe.yygh.cmn.listener.ExcelListener;
import com.gdufe.yygh.cmn.mapper.DictMapper;
import com.gdufe.yygh.cmn.service.DictService;
import com.gdufe.yygh.model.cmn.Dict;
import com.gdufe.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    private Dict getDictByDictCode(String dictCode){
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code",dictCode);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        //根据dictCode获取对应id
        Long parent_id = this.getDictByDictCode(dictCode).getId();
        //根据id获得其子节点
        List<Dict> dicts = baseMapper.selectList(new QueryWrapper<Dict>().eq("parent_id", parent_id));
        return dicts;
    }

    @Override
    @CacheEvict(value = "dict",allEntries = true)
    public void importDictData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(),DictEeVo.class,new ExcelListener(baseMapper)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exportDict(HttpServletResponse response) {
        //设置下载信息
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        //URLEncoder.encode可以防止中文乱码,和EasyExcel没关系
        String fileName = "Dict";
        response.setHeader("Content-disposition","attachment="+fileName+".xlsx");
        //查询数据
        List<Dict> dictList = baseMapper.selectList(null);
        //把Dict转换为DictEeVo
        List<DictEeVo> list = new ArrayList<>();
        for (Dict dic:dictList){
            DictEeVo dictEeVo = new DictEeVo();
            BeanUtils.copyProperties(dic,dictEeVo);
            list.add(dictEeVo);
        }
        //写入Excel
        try {
            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("dict").doWrite(list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //根据数据id查询子数据列表
    @Override
    @Cacheable(value = "dict",keyGenerator = "keyGenerator")
    public List<Dict> findChlidData(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        List<Dict> dictList = baseMapper.selectList(wrapper);
        for (Dict dic:dictList){
            Long dicId = dic.getId();
            boolean flag = this.hasChildren(dicId);
            dic.setHasChildren(flag);
        }
        return dictList;//有@Cacheable注解会把dictList加到缓存中
    }

    //判断是否有子节点
    private boolean hasChildren(Long id){
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        Integer integer = baseMapper.selectCount(wrapper);
        return integer>0;
    }

    @Override
    public String getDictName(String dictCode, String value) {
        Dict dict = new Dict();
        //dictCode为空直接进行根据value查询
        if (StringUtils.isEmpty(dictCode)){
            //根据value查询
            QueryWrapper<Dict> wrapper = new QueryWrapper<>();
            wrapper.eq("value",value);
            dict = baseMapper.selectOne(wrapper);
        }else {
            //根据两个条件查询
            //根据dictcode查询dict对象，得到dict的id 然后查询其子节点中value等于要查询的value值的节点
            Dict CodeDict = this.getDictByDictCode(dictCode);
            Long parentId = CodeDict.getId();
            QueryWrapper<Dict> wrapper = new QueryWrapper<>();
            wrapper.eq("parent_id",parentId).eq("value",value);
            dict = baseMapper.selectOne(wrapper);
        }
        return dict.getName();
    }
}
