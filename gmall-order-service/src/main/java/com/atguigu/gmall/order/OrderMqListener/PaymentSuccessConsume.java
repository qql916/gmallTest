package com.atguigu.gmall.order.OrderMqListener;

import com.atguigu.gmall.user.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

//创建订单系统PaymentSuccess消费端
@Component
public class PaymentSuccessConsume {
    @Autowired
    OrderService orderService;

    @JmsListener(destination = "PAYMENT_SUCCESS_QUEUE", containerFactory = "jmsQueueListener")
    public void consumePaymentResult(MapMessage mapMessage) throws JMSException {

        System.out.println("创建支付成功的消费端");
//        String out_trade_no = mapMessage.getString("out_trade_no");
//        String trade_no = mapMessage.getString("trade_no");
//        String result = mapMessage.getString("result");
//        if ("已支付".equals(result)){
//            orderService.updateProcessStatus(out_trade_no,"支付成功",trade_no);
//        }else {
//            orderService.updateProcessStatus(out_trade_no,"支付失败",trade_no);
//        }
//        //订单系统处理消息
//        orderService.sendOrderResult(out_trade_no);
//    }
        String out_trade_no = mapMessage.getString("out_trade_no");
        String trade_no = mapMessage.getString("trade_no");
        String result = mapMessage.getString("result");
        if (!"已支付".equals(result)) {
            orderService.updateProcessStatus(out_trade_no, "支付失败", trade_no);
        } else {
            orderService.updateProcessStatus(out_trade_no, "支付成功", trade_no);
        }

        // 订单处理消息
        orderService.sendOrderResult(out_trade_no);
    }

}
