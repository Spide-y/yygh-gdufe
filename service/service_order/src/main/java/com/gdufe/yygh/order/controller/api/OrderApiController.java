package com.gdufe.yygh.order.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gdufe.yygh.common.result.Result;
import com.gdufe.yygh.common.utils.AuthContextHolder;
import com.gdufe.yygh.enums.OrderStatusEnum;
import com.gdufe.yygh.model.order.OrderInfo;
import com.gdufe.yygh.order.service.OrderService;
import com.gdufe.yygh.vo.order.OrderCountQueryVo;
import com.gdufe.yygh.vo.order.OrderQueryVo;
import com.gdufe.yygh.vo.user.UserInfoQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderApiController {

    @Resource
    private OrderService orderService;

    //生成挂号订单
    @ApiOperation("生成挂号订单")
    @PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
    public Result saveOrder(@PathVariable String scheduleId,@PathVariable Long patientId){
        Long orderId = orderService.saveOrder(scheduleId,patientId);
        return Result.ok(orderId);
    }

    //根据订单id查询订单详情
    @GetMapping("auth/getOrders/{orderId}")
    public Result getOrders(@PathVariable String orderId){
        OrderInfo orderInfo = orderService.getOrder(orderId);
        return Result.ok(orderInfo);
    }

    //订单列表(条件查询带分页)
    @GetMapping("auth/{page}/{limit}")
    public Result list(@PathVariable Long page, @PathVariable Long limit, OrderQueryVo orderQueryVo, HttpServletRequest req){
        //设置当前用户id
        orderQueryVo.setUserId(AuthContextHolder.getUserId(req));
        Page<OrderInfo> pages = new Page<>(page,limit);
        IPage<OrderInfo> pageModel = orderService.selectPage(pages,orderQueryVo);
        return Result.ok(pageModel);
    }

    //获取订单状态
    @GetMapping("auth/getStatusList")
    public Result getStatusList(){
        return Result.ok(OrderStatusEnum.getStatusList());
    }

    //取消预约
    @GetMapping("auth/cancelOrder/{orderId}")
    public Result cancelOrder(@PathVariable Long orderId){
        Boolean isOrder = orderService.cancelOrder(orderId);
        return Result.ok(isOrder);
    }

    //获取订单统计数量
    @PostMapping("inner/getCountMap")
    public Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo) {
        return orderService.getCountMap(orderCountQueryVo);
    }


}
