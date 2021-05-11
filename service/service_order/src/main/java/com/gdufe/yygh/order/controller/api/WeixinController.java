package com.gdufe.yygh.order.controller.api;

import com.gdufe.yygh.common.result.Result;
import com.gdufe.yygh.order.service.PaymentService;
import com.gdufe.yygh.order.service.WeiXinService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/order/weixin")
public class WeixinController {

    @Resource
    private WeiXinService weiXinService;
    @Resource
    private PaymentService paymentService;

    //生成支付二维码
    @GetMapping("createNative/{orderId}")
    public Result createNative(@PathVariable Long orderId){
        Map map = weiXinService.createNative(orderId);
        return Result.ok(map);
    }

    //查询支付状态
    @GetMapping("queryPayStatus/{orderId}")
    public Result queryPayStatus(@PathVariable Long orderId){
        //调用微信接口进行查询
        Map<String,String> resultMap = weiXinService.queryPayStatus(orderId);
        //判断
        if (resultMap==null){
            return Result.fail().message("支付出错");
        }
        if ("SUCCESS".equals(resultMap.get("trade_state"))){
            //更新订单状态
            String out_trade_no = resultMap.get("out_trade_no");//订单编码
            paymentService.paySuccess(out_trade_no,resultMap);
            return Result.ok().message("支付成功");
        }
        return Result.ok().message("支付中");
    }

}
