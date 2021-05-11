package com.gdufe.easy;

import com.alibaba.excel.EasyExcel;

public class TestRead {
    public static void main(String[] args) {

        //设置excel文件路径和文件名称
        String fileName = "D:\\项目资料\\尚医通\\excel\\01.xlsx";

        EasyExcel.read(fileName,User.class,new ExcelListener()).sheet().doRead();

    }
}
