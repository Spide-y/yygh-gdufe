package com.gdufe.yygh.cmn.controller;

import com.gdufe.yygh.cmn.service.DictService;
import com.gdufe.yygh.common.result.Result;
import com.gdufe.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(description = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")
//@CrossOrigin
public class DictController {

    @Resource
    private DictService dictService;

    //导入数据字典
    @PostMapping("importDict")
    public Result importDict(MultipartFile file){
        dictService.importDictData(file);
        return Result.ok();
    }

    //导出数据字典接口
    @GetMapping("exportDict")
    public void exportDict(HttpServletResponse response){
        dictService.exportDict(response);
    }

    //根据数据id查询子数据列表
    @ApiOperation(value = "根据数据id查询子数据列表")
    @GetMapping("findChildData/{id}")
    public Result findChildData(@PathVariable Long id) {
        List<Dict> list = dictService.findChlidData(id);
        return Result.ok(list);
    }

    //根据dictcode和value查询
    @GetMapping("getName/{dictCode}/{value}")
    public Result getName(@PathVariable String dictCode,@PathVariable String value){
        String dicName = dictService.getDictName(dictCode,value);
        return Result.ok(dicName);
    }

    //根据value查询
    @GetMapping("getName/{value}")
    public Result getName(@PathVariable String value){
        String dicName = dictService.getDictName("",value);
        return Result.ok(dicName);
    }

    //根据dictCode获取下级节点
    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping("findByDictCode/{dictCode}")
    public Result findByDictCode(@PathVariable String dictCode){
        List<Dict> list = dictService.findByDictCode(dictCode);
        return Result.ok(list);
    }


}
