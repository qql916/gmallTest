package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.OrderDetail;
import com.atguigu.gmall.user.bean.OrderInfo;

import java.util.List;

public interface OrderService {
    void putTradeCode(String tradeCode, String userId);

    boolean checkTradeCode(String tradeCode, String userId);


    void saveOrder(OrderInfo orderInfoForDb);

    void removeCheckedCart(List<String> delList);

    OrderInfo getOrderByOutTradeNo(String outTradeNo);

    void updateProcessStatus(String out_trade_no, String result, String trade_no);

    void sendOrderResult(String out_trade_no);
}
