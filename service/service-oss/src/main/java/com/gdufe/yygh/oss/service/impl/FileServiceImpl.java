package com.gdufe.yygh.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.gdufe.yygh.oss.service.FileService;
import com.gdufe.yygh.oss.utils.ConstantOssProperties;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    //上传文件
    @Override
    public String upload(MultipartFile file) {
        try {
            String endpoint = ConstantOssProperties.ENDPOINT;
            // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录RAM控制台创建RAM账号。
            String accessKeyId = ConstantOssProperties.ACCESS_KEY_ID;
            String accessKeySecret = ConstantOssProperties.SECRET;
            String bucketName = ConstantOssProperties.BUCKET;
            System.out.println(accessKeyId);
            // 创建OSSClient实例。
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            //上传文件流
            InputStream input = file.getInputStream();
            String fileName = file.getOriginalFilename();
            //生成唯一id,添加到文件名中
            String uuid = UUID.randomUUID().toString().replaceAll("-","");
            fileName = uuid+fileName;

            //按照当前日期，创建文件夹，上传到文件夹中
            String time = new DateTime().toString("yyyy/MM/dd");
            fileName=time+"/"+fileName;

            //放入oss中
            ossClient.putObject(bucketName,fileName,input);

            // 关闭OSSClient。
            ossClient.shutdown();

            //上传之后的文件路径
            String url = "https://"+bucketName+"."+endpoint+"/"+fileName;
            return url;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
