package com.gdufe.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gdufe.yygh.model.order.PaymentInfo;
import com.gdufe.yygh.model.order.RefundInfo;

public interface RefundInfoService extends IService<RefundInfo> {

    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);

}
