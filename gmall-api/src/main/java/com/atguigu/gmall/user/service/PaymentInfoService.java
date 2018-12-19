package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.PaymentInfo;

import java.util.Map;

public interface PaymentInfoService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    boolean checkPayStatus(String out_trade_no);

    void updatePaymentInfo(PaymentInfo paymentInfo);

    void sendPaymentResult(String orderId, String result);

    void sendPaymentSuccessQueue(PaymentInfo paymentInfo);

    void sendDelayPaymentResult(PaymentInfo paymentInfo, int count);

    Map<String,String> checkAlipayPayment(String out_trade_no);

    PaymentInfo getPaymentInfoByOutTradeNo(String out_trade_no);
}
