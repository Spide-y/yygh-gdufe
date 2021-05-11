package com.gdufe.easy;

import com.alibaba.excel.EasyExcel;

import java.util.ArrayList;
import java.util.List;

public class TestWrite {
    public static void main(String[] args) {

        //设置excel文件路径和文件名称
        String fileName = "D:\\项目资料\\尚医通\\excel\\01.xlsx";

        //设置excel文件路径和文件名称
        List<User> list = new ArrayList();
        for (int i = 0;i<10;i++){
            User user = new User();
            user.setId(i);
            user.setName(i+"Peter");
            list.add(user);
        }

        //调用方法实现写操作
        EasyExcel.write(fileName,User.class).sheet("用户信息").doWrite(list);

    }
}
