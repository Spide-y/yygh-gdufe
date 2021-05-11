package com.gdufe.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gdufe.yygh.common.exception.YyghException;
import com.gdufe.yygh.common.helper.HttpRequestHelper;
import com.gdufe.yygh.common.result.ResultCodeEnum;
import com.gdufe.yygh.enums.OrderStatusEnum;
import com.gdufe.yygh.enums.PaymentStatusEnum;
import com.gdufe.yygh.hosp.client.HospitalFeignClient;
import com.gdufe.yygh.model.order.OrderInfo;
import com.gdufe.yygh.model.order.PaymentInfo;
import com.gdufe.yygh.order.mapper.PaymentMapper;
import com.gdufe.yygh.order.service.OrderService;
import com.gdufe.yygh.order.service.PaymentService;
import com.gdufe.yygh.vo.order.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService {

    @Resource
    private OrderService orderService;
    @Resource
    private HospitalFeignClient hospitalFeignClient;

    //获取支付记录
    @Override
    public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id",orderId);
        wrapper.eq("payment_type",paymentType);
        PaymentInfo paymentInfo = baseMapper.selectOne(wrapper);
        return paymentInfo;
    }

    //更新订单状态
    @Override
    public void paySuccess(String out_trade_no, Map<String, String> resultMap) {
        //获取支付记录
        PaymentInfo paymentInfo = baseMapper.selectOne(new QueryWrapper<PaymentInfo>().eq("out_trade_no", out_trade_no));

        //更新支付记录
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setTradeNo(resultMap.get("transaction_id"));
        paymentInfo.setCallbackContent(resultMap.toString());
        baseMapper.updateById(paymentInfo);

        //获取订单信息并更新订单信息
        OrderInfo orderInfo = orderService.getById(paymentInfo.getOrderId());
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderService.updateById(orderInfo);

        //调用医院接口，更新订单支付信息
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode",orderInfo.getHoscode());
        reqMap.put("hosRecordId",orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);
        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl()+"/order/updatePayStatus");
        if(result.getInteger("code") != 200) {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }


    }

    //查询是否存在该记录
    @Override
    public void savePayment(OrderInfo orderInfo, Integer status) {
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id",orderInfo.getId());
        wrapper.eq("payment_type",status);
        Integer count = baseMapper.selectCount(wrapper);
        if (count>0){
            return;
        }else {
            //添加记录
            PaymentInfo payment = new PaymentInfo();
            payment.setCreateTime(new Date());
            payment.setOrderId(orderInfo.getId());
            payment.setPaymentType(status);
            payment.setOutTradeNo(orderInfo.getOutTradeNo());
            System.out.println("********"+PaymentStatusEnum.UNPAID.getStatus()+"*******");
            payment.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
            String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")+"|"+orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle();
            payment.setSubject(subject);
            payment.setTotalAmount(orderInfo.getAmount());
            baseMapper.insert(payment);

        }
    }
}
