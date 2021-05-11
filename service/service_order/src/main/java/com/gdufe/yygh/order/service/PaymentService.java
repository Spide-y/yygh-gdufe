package com.gdufe.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gdufe.yygh.model.order.OrderInfo;
import com.gdufe.yygh.model.order.PaymentInfo;

import java.util.Map;

public interface PaymentService extends IService<PaymentInfo> {
    void savePayment(OrderInfo orderInfo, Integer status);

    void paySuccess(String out_trade_no, Map<String, String> resultMap);

    PaymentInfo getPaymentInfo(Long orderId,Integer paymentType);

}
