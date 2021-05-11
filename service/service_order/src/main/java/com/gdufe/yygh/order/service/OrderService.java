package com.gdufe.yygh.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gdufe.yygh.model.order.OrderInfo;
import com.gdufe.yygh.vo.order.OrderCountQueryVo;
import com.gdufe.yygh.vo.order.OrderCountVo;
import com.gdufe.yygh.vo.order.OrderQueryVo;

import java.util.Map;

public interface OrderService extends IService<OrderInfo> {
    Long saveOrder(String scheduleId, Long patientId);

    OrderInfo getOrder(String orderId);

    IPage<OrderInfo> selectPage(Page<OrderInfo> pages, OrderQueryVo orderQueryVo);

    Boolean cancelOrder(Long orderId);

    void patientTips();

    Map<String ,Object> getCountMap(OrderCountQueryVo orderCountVo);
}
