package com.gdufe.yygh.order.service;

import java.util.Map;

public interface WeiXinService {
    Map createNative(Long orderId);

    Map<String, String> queryPayStatus(Long orderId);

    Boolean refund(Long orderId);
}
