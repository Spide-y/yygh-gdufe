package com.gdufe.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.gdufe.yygh.cmn.mapper.DictMapper;
import com.gdufe.yygh.model.cmn.Dict;
import com.gdufe.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;


public class ExcelListener extends AnalysisEventListener<DictEeVo> {

    private DictMapper dictMapper;

    public ExcelListener(DictMapper dictMapper){
        this.dictMapper = dictMapper;
    }

    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        //调用方法添加数据库
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo,dict);
        dictMapper.insert(dict);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
