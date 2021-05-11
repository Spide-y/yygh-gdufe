package com.gdufe.easy;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class User {
    @ExcelProperty("用户编号")
    private int id;
    @ExcelProperty("用户名称")
    private String name;
}
