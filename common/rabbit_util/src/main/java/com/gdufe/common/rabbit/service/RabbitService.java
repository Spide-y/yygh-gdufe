package com.gdufe.common.rabbit.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RabbitService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /***
     * exchange 交换机
     * routingKey 路由键
     * message 消息
     */

    public boolean sendMessage(String exchange,String routingKey,Object message){
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
        return true;
    }

}
