package com.atguigu.gmall.payment.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;

import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.user.bean.PaymentInfo;
import com.atguigu.gmall.user.service.PaymentInfoService;
import com.atguigu.gmall.utils.ActiveMQUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class paymentInfoServiceImpl implements PaymentInfoService {
    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        int i = paymentInfoMapper.insertSelective(paymentInfo);
        System.out.println(i);
    }

    /**
     * 检查订单支付状态，true为已支付，false未支付，幂等性检查
     *
     * @param out_trade_no
     * @return
     */
    @Override
    public boolean checkPayStatus(String out_trade_no) {
        boolean b = true;
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(out_trade_no);
        PaymentInfo paymentInfoDb = paymentInfoMapper.selectOne(paymentInfo);
        if (paymentInfoDb != null && paymentInfoDb.getPaymentStatus().equals("未支付")) {
            b = false;
        }

        return b;
    }

    @Override
    public void updatePaymentInfo(PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo", paymentInfo.getOutTradeNo());
        paymentInfoMapper.updateByExampleSelective(paymentInfo, example);
    }

    //测试
    @Override
    public void sendPaymentResult(String orderId, String result) {


        try {
            Connection connection = activeMQUtil.getConnection();
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue paymentResultQueue = session.createQueue("PAYMENT_RESULT_QUEUE");
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("orderId", orderId);
            mapMessage.setString("result", result);
            MessageProducer producer = session.createProducer(paymentResultQueue);
            producer.send(mapMessage);
            session.commit();
            producer.close();
            session.close();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendPaymentSuccessQueue(PaymentInfo paymentInfo) {
        // 发送支付成功的消息队列
        // 建立mq工厂
        try {
            Connection connection = activeMQUtil.getConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testQueue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(testQueue);
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            //将要通知的信息存入
            mapMessage.setString("out_trade_no", paymentInfo.getOutTradeNo());//外部订单号
            mapMessage.setString("trade_no", paymentInfo.getAlipayTradeNo());//支付宝交易返回的订单号
            mapMessage.setString("result", paymentInfo.getPaymentStatus());
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);//发送信息
            session.commit();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 启动延迟检查支付状态的队列
    @Override
    public void sendDelayPaymentResult(PaymentInfo paymentInfo, int count) {
        //发送延迟支付的消息队列
        // 建立mq工厂
        try {
            Connection connection = activeMQUtil.getConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue testQueue = session.createQueue("PAYMENT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(testQueue);
            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            //将要通知的信息存入
            mapMessage.setString("out_trade_no", paymentInfo.getOutTradeNo());//外部订单号
            mapMessage.setInt("count", count);
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 1000 * 20);//设置发送延迟队列的时间间隔
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.send(mapMessage);//发送信息
            session.commit();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //检查支付状态的实现方法
    @Override
    public Map<String, String> checkAlipayPayment(String out_trade_no) {
        //创建用来返回的数据的集合
        Map<String,String> returnMap  = new HashMap<>();
        System.out.println("调用检查支付接口");
        //创建统一收单线下交易查询接口
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest ();

        Map<String,String> requestMap  = new HashMap<>();
        requestMap .put("out_trade_no",out_trade_no);

        request.setBizContent(JSON.toJSONString(requestMap ));
        AlipayTradeQueryResponse  response = null;

        try {
            response =  alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if(response.isSuccess()){
            System.out.println("调用成功");
            //从支付宝中返回的response获取需要的数据
            String tradeNo = response.getTradeNo();
            String tradeStatus = response.getTradeStatus();
            String queryString = response.toString();
            returnMap .put("trade_no",tradeNo);
            returnMap .put("trade_status",tradeStatus);
            returnMap .put("queryString",queryString);
        } else {
            System.out.println("调用失败");
        }
        return returnMap;
    }

    //根据外部订单号查询订单支付信息
    @Override
    public PaymentInfo getPaymentInfoByOutTradeNo(String out_trade_no) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(out_trade_no);
        PaymentInfo paymentInfoDb = paymentInfoMapper.selectOne(paymentInfo);
        return paymentInfoDb;
    }
}