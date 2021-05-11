package com.gdufe.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gdufe.common.rabbit.constant.MqConst;
import com.gdufe.common.rabbit.service.RabbitService;
import com.gdufe.yygh.common.exception.YyghException;
import com.gdufe.yygh.common.helper.HttpRequestHelper;
import com.gdufe.yygh.common.result.ResultCodeEnum;
import com.gdufe.yygh.enums.OrderStatusEnum;
import com.gdufe.yygh.hosp.client.HospitalFeignClient;
import com.gdufe.yygh.model.order.OrderInfo;
import com.gdufe.yygh.model.user.Patient;
import com.gdufe.yygh.order.mapper.OrderMapper;
import com.gdufe.yygh.order.service.OrderService;
import com.gdufe.yygh.order.service.WeiXinService;
import com.gdufe.yygh.user.client.PatientFeignClient;
import com.gdufe.yygh.vo.hosp.ScheduleOrderVo;
import com.gdufe.yygh.vo.msm.MsmVo;
import com.gdufe.yygh.vo.order.*;
import io.swagger.models.auth.In;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderInfo> implements OrderService {

    @Resource
    private PatientFeignClient patientFeignClient;
    @Resource
    private HospitalFeignClient hospitalFeignClient;
    @Resource
    private RabbitService rabbitService;
    @Resource
    private WeiXinService weiXinService;

    //预约统计
    @Override
    public Map<String, Object> getCountMap(OrderCountQueryVo orderCountVo) {
        //调用mapper获取数据
        List<OrderCountVo> orderCountVoList = baseMapper.selectOrderCount(orderCountVo);
        //获取x需要数据
        List<String> dateList = orderCountVoList.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());

        //获取y需要数据
        List<Integer> countList = orderCountVoList.stream().map(OrderCountVo::getCount).collect(Collectors.toList());

        Map<String,Object> map = new HashMap<>();
        map.put("dateList", dateList);
        map.put("countList", countList);
        return map;

    }

    //包装订单信息
    private OrderInfo packOrder(OrderInfo orderInfo){
        orderInfo.getParam().put("orderStatusString",OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }

    //就诊通知
    @Override
    public void patientTips() {
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("reserve_date",new Date());
        wrapper.ne("order_status",OrderStatusEnum.CANCLE.getStatus());
        List<OrderInfo> orderInfoList = baseMapper.selectList(wrapper);
        for (OrderInfo order:orderInfoList){
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(order.getPatientPhone());
            String reserveDate = new DateTime(order.getReserveDate()).toString("yyyy-MM-dd")+(order.getReserveTime()==0?"上午":"下午");
            Map<String,Object> param = new HashMap<String, Object>(){{
                put("title",order.getHosname()+"|"+order.getDepname()+"|"+order.getTitle());
                put("reserveDate",reserveDate);
                put("name",order.getPatientName());
            }};
            msmVo.setParam(param);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM,MqConst.ROUTING_MSM_ITEM,msmVo);
        }
    }

    //取消预约
    @Override
    public Boolean cancelOrder(Long orderId) {
        //获取订单信息
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        //判断是否可以取消
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
        if (quitTime.isBeforeNow()){
            throw new YyghException(ResultCodeEnum.CANCEL_ORDER_NO);
        }
        //调用医院接口
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        if(null == signInfoVo) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode",orderInfo.getHoscode());
        reqMap.put("hosRecordId",orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);

        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl()+"/order/updateCancelStatus");

        if(result.getInteger("code") != 200) {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        } else {
            //是否支付 退款
            if(orderInfo.getOrderStatus().intValue() == OrderStatusEnum.PAID.getStatus().intValue()) {
            //已支付 退款
                boolean isRefund = weiXinService.refund(orderId);
                if(!isRefund) {
                    throw new YyghException(ResultCodeEnum.CANCEL_ORDER_FAIL);
                }
            }
            //更改订单状态
            orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
            baseMapper.updateById(orderInfo);
            //发送mq信息更新预约数 我们与下单成功更新预约数使用相同的mq信息，不设置可预约数与剩余预约数，接收端可预约数减1即可
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(orderInfo.getScheduleId());
            //短信提示
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            msmVo.setTemplateCode("SMS_194640722");
            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "上午": "下午");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
            }};
            msmVo.setParam(param);
            orderMqVo.setMsmVo(msmVo);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
        }
        return true;
    }


    //订单列表
    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> pages, OrderQueryVo orderQueryVo) {
        //根据vo获得条件值
        String keyword = orderQueryVo.getKeyword();//医院名称
        String patientName = orderQueryVo.getPatientName();//病人名字
        String orderStatus = orderQueryVo.getOrderStatus();//订单状态
        String reserveDate = orderQueryVo.getReserveDate();//安排时间
        String createTime = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();

        //进行非空判断
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(keyword)){
            wrapper.like("hosname",keyword);
        }
        if (!StringUtils.isEmpty(patientName)){
            wrapper.like("patient_name",patientName);
        }
        if (!StringUtils.isEmpty(orderStatus)){
            wrapper.like("order_status",orderStatus);
        }
        if (!StringUtils.isEmpty(reserveDate)){
            wrapper.like("reserve_date",reserveDate);
        }
        if (!StringUtils.isEmpty(createTime)){
            wrapper.ge("create_time",createTime);
        }
        if (!StringUtils.isEmpty(createTimeEnd)){
            wrapper.le("create_time",createTimeEnd);
        }

        //调用mapper
        IPage<OrderInfo> orderInfoIPage = baseMapper.selectPage(pages,wrapper);
        //编号变成对应值封装
        orderInfoIPage.getRecords().stream().forEach(item->{
            this.packOrder(item);
        });

        return orderInfoIPage;
    }

    //获取订单详情
    @Override
    public OrderInfo getOrder(String orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        return this.packOrder(orderInfo);
    }

    //保存订单
    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        //获取就诊人信息
        Patient patient = patientFeignClient.getPatientOrder(patientId);
        //获取排班信息
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);
        //判断当前时间是否可预约
        if (new DateTime(scheduleOrderVo.getStartTime()).isAfterNow() || new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()){
            throw new YyghException(ResultCodeEnum.TIME_NO);
        }

        //获取签命信息
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(scheduleOrderVo.getHoscode());

        //添加到表中
        OrderInfo orderInfo = new OrderInfo();
        //数据复制
        BeanUtils.copyProperties(scheduleOrderVo,orderInfo);
        //完善orderInfo
        String outTradeNo = System.currentTimeMillis()+""+new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setScheduleId(scheduleId);
        orderInfo.setPatientId(patientId);
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
        baseMapper.insert(orderInfo);

        //调用医院接口，实现预约挂号操作
        //设置调用医院接口需要参数，参数放到map集合中
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",orderInfo.getHoscode());
        paramMap.put("depcode",orderInfo.getDepcode());
        paramMap.put("hosScheduleId",orderInfo.getScheduleId());
        paramMap.put("reserveDate",new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", orderInfo.getReserveTime());
        paramMap.put("amount",orderInfo.getAmount());
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType",patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex",patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone",patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode",patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode",patient.getDistrictCode());
        paramMap.put("address",patient.getAddress());
        //联系人
        paramMap.put("contactsName",patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo",patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone",patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        //String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());//给sign加密
        paramMap.put("sign", signInfoVo.getSignKey());

        //请求医院接口
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, signInfoVo.getApiUrl() + "/order/submitOrder");

        if (result.getInteger("code")==200){
            JSONObject jsonObject = result.getJSONObject("data");
            //预约记录唯一标识（医院预约记录主键）
            String hosRecordId = jsonObject.getString("hosRecordId");
            //预约序号
            Integer number = jsonObject.getInteger("number");;
            //取号时间
            String fetchTime = jsonObject.getString("fetchTime");;
            //取号地址
            String fetchAddress = jsonObject.getString("fetchAddress");;
            //更新订单
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);
            baseMapper.updateById(orderInfo);

            //排班可预约数
            Integer reservedNumber = jsonObject.getInteger("reservedNumber");
            //排班剩余预约数
            Integer availableNumber = jsonObject.getInteger("availableNumber");
            //发送mq信息更新号源和短信通知
            //发送mq进行号源的更新
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(scheduleId);
            orderMqVo.setReservedNumber(reservedNumber);
            orderMqVo.setAvailableNumber(availableNumber);
            //短信提示
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            msmVo.setTemplateCode("SMS_194640721");
            String reserveDate =
                    new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                            + (orderInfo.getReserveTime()==0 ? "上午": "下午");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                put("amount", orderInfo.getAmount());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
                put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
            }};
            msmVo.setParam(param);

            orderMqVo.setMsmVo(msmVo);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);



        }else {
            throw new YyghException(result.getString("message"),ResultCodeEnum.FAIL.getCode());
        }

        return orderInfo.getId();
    }

}
