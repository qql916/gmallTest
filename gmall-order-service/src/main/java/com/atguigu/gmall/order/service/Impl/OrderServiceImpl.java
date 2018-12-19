package com.atguigu.gmall.order.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.user.bean.OrderDetail;
import com.atguigu.gmall.user.bean.OrderInfo;
import com.atguigu.gmall.user.bean.PaymentInfo;
import com.atguigu.gmall.user.service.OrderService;
import com.atguigu.gmall.user.utils.Acconst;
import com.atguigu.gmall.utils.ActiveMQUtil;
import com.atguigu.gmall.utils.RedisUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Override
    public void putTradeCode(String tradeCode, String userId) {
        Jedis jedis = redisUtil.getJedis();
        String setex = jedis.setex(Acconst.USER_KEY_PREFIX + userId + Acconst.TRADE_CODE, Acconst.TRADE_CODE_TIME, tradeCode);
        System.out.println(setex);
        jedis.close();
    }

    @Override
    public boolean checkTradeCode(String tradeCode, String userId) {
        boolean b = false;
        Jedis jedis = redisUtil.getJedis();
        String tradeCodeCache = jedis.get(Acconst.USER_KEY_PREFIX+userId+Acconst.TRADE_CODE);
        if (StringUtils.isNotBlank(tradeCodeCache)){
            if (tradeCode.equals(tradeCodeCache)){
                jedis.del(Acconst.USER_KEY_PREFIX + userId + Acconst.TRADE_CODE);
                b = true;
            }
        }
        jedis.close();
        return b;
    }

    @Override
    public void saveOrder(OrderInfo orderInfoForDb) {
        //新增订单
        orderInfoMapper.insertSelective(orderInfoForDb);
        String orderInfoForDbId = orderInfoForDb.getId();

        //新增订单详情
        List<OrderDetail> orderDetailList = orderInfoForDb.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfoForDbId);
            orderDetailMapper.insertSelective(orderDetail);
        }

    }

    @Override
    public void removeCheckedCart(List<String> delList) {
        String delStr = StringUtils.join(delList, ",");//分割集合，变成字符串
        orderInfoMapper.deleteCheckedCart(delStr);
    }

    @Override
    public OrderInfo getOrderByOutTradeNo(String outTradeNo) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOutTradeNo(outTradeNo);
        OrderInfo orderInfoDb = orderInfoMapper.selectOne(orderInfo);
        //封装OrderDetailList
        String orderInfoDbId = orderInfoDb.getId();
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderInfoDbId);
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);
        orderInfoDb.setOrderDetailList(orderDetails);
        return orderInfoDb;
    }

    @Override
    public void updateProcessStatus(String out_trade_no, String result, String trade_no) {
        // 更新订单的状态，流程状态，支付宝流水号
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",out_trade_no);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderStatus("订单已支付");
        orderInfo.setProcessStatus("订单已支付");
        orderInfo.setTrackingNo(trade_no);
        orderInfoMapper.updateByExampleSelective(orderInfo,example);

    }

    @Override
    public void sendOrderResult(String out_trade_no) {
        // 发送订单结果的消息通知给系统
        // 发送订单支付成功的消息队列
        // 建立mq工厂
        try {
            Connection connection = activeMQUtil.getConnection();
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testQueue = session.createQueue("ORDER_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(testQueue);
            ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
            //根据外部订单号获取订单信息
            OrderInfo orderInfo = getOrderByOutTradeNo(out_trade_no);
            activeMQTextMessage.setText(JSON.toJSONString(orderInfo));
//            通过producer.setDeliveryMode(DeliveryMode.PERSISTENT) 进行设置
//            持久化的好处就是当activemq宕机的话，消息队列中的消息不会丢失。非持久化会丢失。但是会消耗一定的性能。
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(activeMQTextMessage);
            session.commit();
            connection.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }


}
