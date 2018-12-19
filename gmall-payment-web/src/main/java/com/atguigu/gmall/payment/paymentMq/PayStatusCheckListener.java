package com.atguigu.gmall.payment.paymentMq;

import com.atguigu.gmall.user.bean.PaymentInfo;
import com.atguigu.gmall.user.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

/**
 * PAYMENT_CHECK_QUEUE的消费端
 */
@Component
public class PayStatusCheckListener {
    @Autowired
    PaymentInfoService paymentInfoService;
    //destination监听的消息队列
    @JmsListener(destination = "PAYMENT_CHECK_QUEUE", containerFactory = "jmsQueueListener")
    public void consumeCheckResult(MapMessage mapMessage) throws JMSException {
        //获取检查次数
        int count = mapMessage.getInt("count");
        String out_trade_no = mapMessage.getString("out_trade_no");
        //检查支付状态
        System.err.println("开始检查支付状态：第"+(6-count)+"次");
        Map<String,String> success = paymentInfoService.checkAlipayPayment(out_trade_no);
        String trade_no = success.get("trade_no");
        String trade_status = success.get("trade_status");
        String queryString = success.get("queryString");
        count--;

        // 继续发送延迟队列(1 count值大于0，2 查询到的支付状态为未支付成功)
        if (trade_status!=null&&(trade_status.equals("TRADE_SUCCESS")||trade_status.equals("TRADE_FINISHED"))){
            //如果支付状态不为空且等于TRADE_SUCCESS或TRADE_FINISHED，则更新支付信息
            //1.先进行幂等性检查，false就更新
            boolean b = paymentInfoService.checkPayStatus(out_trade_no);
            if (!b){
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setAlipayTradeNo(trade_no);
                paymentInfo.setPaymentStatus("已支付");
                paymentInfo.setCallbackTime(new Date());
                paymentInfo.setCallbackContent(queryString);
                paymentInfo.setOutTradeNo(out_trade_no);
                paymentInfoService.updatePaymentInfo(paymentInfo);
                //发送支付成功的队列，通知订单系统
                paymentInfoService.sendPaymentSuccessQueue(paymentInfo);
            }else{
                //2.true,就不保存
                System.out.println("已经更新过订单！");
            }
        }else{

            //为空或其他状态继续发送延迟队列检查任务，直到轮询次数为0
            // 判断count次数
            if(count>0){
                System.err.println("未支付成功，继续发送下一次的延迟检查任务，剩余次数："+count);
                //1.count>0,根据out_trade_no 查询出paymentInfo信息
                PaymentInfo paymentInfo = paymentInfoService.getPaymentInfoByOutTradeNo(out_trade_no);
                // 2.再发送延迟队列检查任务
                paymentInfoService.sendDelayPaymentResult(paymentInfo,count);
            }else {
                System.out.println("次数用尽，停止延迟检查");
            }
        }
    }
}
