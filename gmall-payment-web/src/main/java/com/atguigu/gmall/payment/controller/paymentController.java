package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;

import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.payment.conf.AlipayConfig;
import com.atguigu.gmall.user.bean.OrderInfo;
import com.atguigu.gmall.user.bean.PaymentInfo;
import com.atguigu.gmall.user.service.OrderService;
import com.atguigu.gmall.user.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class paymentController {

    @Autowired
    AlipayClient alipayClient;

    @Reference
    OrderService orderService;

    @Autowired
    PaymentInfoService paymentInfoService;

    @RequestMapping("index")
    public String paymentIndex(String outTradeNo, BigDecimal totalAmount, String nickName, ModelMap map) {
        map.put("outTradeNo", outTradeNo);
        map.put("totalAmount", totalAmount);
        map.put("nickName", nickName);

        return "index";
    }

    /**
     * 进行支付操作
     *
     * @param outTradeNo
     * @param totalAmount
     * @param map
     * @return
     */
    @RequestMapping("/alipay/submit")
    @ResponseBody
    public String alipay(String outTradeNo, BigDecimal totalAmount, ModelMap map) {
        map.put("outTradeNo", outTradeNo);
        map.put("totalAmount", totalAmount);
        //根据订单号获取订单信息
        OrderInfo orderInfo = orderService.getOrderByOutTradeNo(outTradeNo);

        // 保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setOutTradeNo(outTradeNo);
        paymentInfo.setTotalAmount(totalAmount);
        paymentInfo.setPaymentStatus("未支付");
        String skuName = orderInfo.getOrderDetailList().get(0).getSkuName();
        paymentInfo.setSubject(skuName);


        paymentInfoService.savePaymentInfo(paymentInfo);

        // 对接支付宝的pagePay接口，公共参数
        //1.创建API对应的request
        AlipayTradePagePayRequest alipayTradePagePayRequest = new AlipayTradePagePayRequest();
        alipayTradePagePayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        ////在公共参数中设置回跳和通知地址
        alipayTradePagePayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        //填充业务参数
        Map<String, String> aliMap = new HashMap<>();
        aliMap.put("out_trade_no", outTradeNo);
        aliMap.put("product_code", "FAST_INSTANT_TRADE_PAY");
        aliMap.put("total_amount", "0.01");
        aliMap.put("subject", skuName);
        alipayTradePagePayRequest.setBizContent(JSON.toJSONString(aliMap));

        String form = "";
        try {
            form = alipayClient.pageExecute(alipayTradePagePayRequest).getBody();//调用SDK生成表单
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(form);

        // 启动延迟检查支付状态的队列
        paymentInfoService.sendDelayPaymentResult(paymentInfo, 5);
        return form;


//        return "formError";

    }

    //这里requestMapping对应的路径必须与之前传给支付宝的alipayRequest.setReturnUrl保持一致。
    //alipay/callback/return
    @RequestMapping("alipay/callback/return")
    public String callbackReturn(HttpServletRequest request) {
        // 付款成功 // 修改支付状态
        //获取支付成功后的相关参数
        String notify_time = request.getParameter("notify_time");
        String app_id = request.getParameter("app_id");
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String total_amount = request.getParameter("total_amount");
        // 幂等性检查
        boolean b = paymentInfoService.checkPayStatus(out_trade_no);
        //
        if (!b) {
            //验证阿里签名
            try {
                Map<String, String> testMap = new HashMap<>();
                boolean signVerified = AlipaySignature.rsaCheckV1(testMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);//调用SDK验证签名
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }
//            // 更新支付信息
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setCallbackContent(request.toString());
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setOutTradeNo(out_trade_no);
            paymentInfoService.updatePaymentInfo(paymentInfo);
//            // 通知订单系统，修改订单状态
            paymentInfoService.sendPaymentSuccessQueue(paymentInfo);

        }

        return "finish";
    }

    //测试消息队列
    @ResponseBody
    @RequestMapping("sendPaymentResult")
    public String sendPaymentResult(@RequestParam("orderId") String orderId) {
        paymentInfoService.sendPaymentResult(orderId, "success");
        return "had been sent";
    }
}
