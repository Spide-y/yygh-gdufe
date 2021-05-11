package com.gdufe.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.gdufe.yygh.enums.PaymentTypeEnum;
import com.gdufe.yygh.enums.RefundStatusEnum;
import com.gdufe.yygh.model.order.OrderInfo;
import com.gdufe.yygh.model.order.PaymentInfo;
import com.gdufe.yygh.model.order.RefundInfo;
import com.gdufe.yygh.order.service.OrderService;
import com.gdufe.yygh.order.service.PaymentService;
import com.gdufe.yygh.order.service.RefundInfoService;
import com.gdufe.yygh.order.service.WeiXinService;
import com.gdufe.yygh.order.utils.ConstantPropertiesUtils;
import com.gdufe.yygh.order.utils.HttpClient;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WeiXinServiceImpl implements WeiXinService {

    @Resource
    private OrderService orderService;
    @Resource
    private PaymentService paymentService;
    @Resource(name = "redisTemplate")
    private RedisTemplate template;
    @Resource
    private RefundInfoService refundInfoService;

    //微信退款操作
    @Override
    public Boolean refund(Long orderId) {

        try {
            //查询相关信息()
            PaymentInfo paymentInfo = paymentService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
            //添加信息到退款表
            RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfo);
            //判断当前订单是否已退款
            if (refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus().intValue()){
                return true;
            }
            //调用微信接口
            //封装需要参数
            Map<String,String> paramMap = new HashMap<>();
            paramMap.put("appid",ConstantPropertiesUtils.APPID);       //公众账号ID
            paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER);   //商户编号
            paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
            paramMap.put("transaction_id",paymentInfo.getTradeNo()); //微信订单号
            paramMap.put("out_trade_no",paymentInfo.getOutTradeNo()); //商户订单编号
            paramMap.put("out_refund_no","tk"+paymentInfo.getOutTradeNo()); //商户退款单号
            //paramMap.put("total_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
            //paramMap.put("refund_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee","1");
            paramMap.put("refund_fee","1");
            String paramXml = WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            client.setXmlParam(paramXml);
            client.setHttps(true);
            client.setCert(true);
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);
            client.post();
            //接收返回数据
            String xml = client.getContent();
            Map<String,String> resultMap = WXPayUtil.xmlToMap(xml);
            if (null!=resultMap && WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))){
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundInfoService.updateById(refundInfo);
                return true;
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //查询支付状态
    @Override
    public Map<String, String> queryPayStatus(Long orderId) {
        try {
            //根据id获取订单信息
            OrderInfo orderInfo = orderService.getById(orderId);

            //封装提交数据
            Map paramMap = new HashMap<>();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());


            //设置请求内容
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();

            //得到微信接口返回数据
            String xml = client.getContent();
            Map<String,String> resultMap = WXPayUtil.xmlToMap(xml);
            System.out.println("resultMap: "+resultMap);

            //返回接口数据
            return resultMap;

        }catch (Exception e){

        }
        return null;
    }

    //生成微信支付二维码
    @Override
    public Map createNative(Long orderId) {
        try {
            //先从redis中获取数据
            Map payMap = (Map) template.opsForValue().get(orderId.toString());
            if (payMap!=null){
                return payMap;
            }
            //根据orderId查询order
            OrderInfo orderInfo = orderService.getById(orderId);
            //向支付记录表中添加信息
            paymentService.savePayment(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());
            //设置参数调用微信生成二维码接口
            //把参数转换成xml格式，使用商户key进行加密
            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            String body = orderInfo.getReserveDate() + "就诊"+ orderInfo.getDepname();
            paramMap.put("body", body);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee", "1");//为了测试 统一值
            paramMap.put("spbill_create_ip", "127.0.0.1");
            paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
            paramMap.put("trade_type", "NATIVE");
            //HTTPClient来根据URL访问第三方接口并且传递参数
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //设置map参数
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            //返回相关数据
            String xml = client.getContent();
            //转换成map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            System.out.println(resultMap+"===========");
            //4、封装返回结果集
            Map map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", orderInfo.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url"));//二维码地址

            if (resultMap.get("result_code")!=null){
                template.opsForValue().set(orderId.toString(),map,120, TimeUnit.MINUTES);
            }

            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
