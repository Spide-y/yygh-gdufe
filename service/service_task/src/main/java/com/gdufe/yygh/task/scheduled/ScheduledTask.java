package com.gdufe.yygh.task.scheduled;

import com.gdufe.common.rabbit.constant.MqConst;
import com.gdufe.common.rabbit.service.RabbitService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@EnableScheduling
public class ScheduledTask {

    @Resource
    private RabbitService rabbitService;

    //每天8点执行方法
    //cron表达式: 设置执行间隔   //网站可以在线生成
    @Scheduled(cron = "0/30 * * * * ?")
    public void taskPatient(){
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_8,"");
    }

}
