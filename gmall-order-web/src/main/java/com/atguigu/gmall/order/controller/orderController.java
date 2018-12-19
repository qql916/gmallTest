package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequire;
import com.atguigu.gmall.user.bean.CartInfo;
import com.atguigu.gmall.user.bean.OrderDetail;
import com.atguigu.gmall.user.bean.OrderInfo;
import com.atguigu.gmall.user.bean.UserAddress;
import com.atguigu.gmall.user.bean.enums.PaymentWay;
import com.atguigu.gmall.user.service.CartService;
import com.atguigu.gmall.user.service.OrderService;
import com.atguigu.gmall.user.service.UserService;
import com.atguigu.gmall.user.utils.Acconst;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class orderController {
    @Reference
    UserService userService;

    @Reference
    CartService cartService;

    @Reference
    OrderService orderService;
    //去结算界面
    @LoginRequire(isNeededSuccess = true)//不一定要登录
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request, ModelMap map) {
        String userId = (String) request.getAttribute("userId");
        String nickName = (String) request.getAttribute("nickName");
        //通过id查询用户所有的地址信息
           List<UserAddress> addressListByUserId =  userService.getUserAddressListById(userId);
        //查询商品清单
        List<CartInfo> cartListByUserId = cartService.getCartListByUserId(userId);
        //将购物车中被选中的商品封装到orderDetailList
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartInfo info : cartListByUserId) {
            if (info.getIsChecked().equals("1")){
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setImgUrl(info.getImgUrl());
                orderDetail.setSkuName(info.getSkuName());
                orderDetail.setSkuNum(info.getSkuNum());
                orderDetail.setOrderPrice(info.getCartPrice());
                orderDetail.setSkuId(info.getSkuId());
                orderDetails.add(orderDetail);
            }
        }
        map.put("userAddressList",addressListByUserId);
        map.put("nickName",nickName);
        map.put("orderDetailList",orderDetails);
        map.put("totalAmount",getCartSum(cartListByUserId));

        //生成订单交易码
        String tradeCode = UUID.randomUUID().toString();
        //将交易码存入缓存中
        orderService.putTradeCode(tradeCode,userId);
        map.put("tradeCode",tradeCode);
        return "trade";

    }

    //生成订单和支付
    @LoginRequire(isNeededSuccess = true)
    @RequestMapping("submitOrder")
    public String submitOrder(HttpServletRequest request, ModelMap map, String tradeCode, String addressId,OrderInfo orderInfo){
        String userId = (String)request.getAttribute("userId");
        String nickName = (String)request.getAttribute("nickName");

        // 防止订单的重复提交,true为已生成交易码,能进行交易
        boolean b = orderService.checkTradeCode(tradeCode,userId);
        if (b){
            //需要被删除的购物车的集合
            List<String> delList = new ArrayList<>();
            //获取当前地址的用户信息
            UserAddress userAddress = userService.getUserAddressByAddressId(addressId);
            // 生成订单和订单详情数据,db// 删除购物车数据
            //1.先从数据库中查询用户的购物车中所有商品信息
            List<CartInfo> cartListByUserId = cartService.getCartListByUserId(userId);
            OrderInfo orderInfoForDb = new OrderInfo();
            //外部订单号
            // atguigugmall+毫秒时间戳字符串+订单生成的时间字符串
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String format = sdf.format(new Date());
            orderInfoForDb.setOutTradeNo(Acconst.TRADE_NO_PREFIX+System.currentTimeMillis()+format);
            orderInfoForDb.setOrderStatus("订单已提交");
            orderInfoForDb.setProcessStatus("订单已提交");
            Calendar calendar = Calendar.getInstance();
            //订单过期时间
            calendar.add(Calendar.DATE,1);//当前日期，有效期一天
            orderInfoForDb.setExpireTime(calendar.getTime());
            orderInfoForDb.setConsignee(userAddress.getConsignee());
            orderInfoForDb.setConsigneeTel(userAddress.getPhoneNum());
            orderInfoForDb.setDeliveryAddress(userAddress.getUserAddress());
            orderInfoForDb.setCreateTime(new Date());
            orderInfoForDb.setOrderComment("硅谷订单");
            orderInfoForDb.setPaymentWay(PaymentWay.ONLINE);
            orderInfoForDb.setTotalAmount(getCartSum(cartListByUserId));
            orderInfoForDb.setUserId(userId);
            //创建订单详情集合
            List<OrderDetail> orderDetails = new ArrayList<>();
            for (CartInfo info : cartListByUserId) {
                if (info.getIsChecked().equals("1")){
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setSkuId(info.getSkuId());
                    orderDetail.setSkuNum(info.getSkuNum());
                    orderDetail.setImgUrl(info.getImgUrl());
                    orderDetail.setOrderPrice(info.getCartPrice());
                    orderDetail.setSkuName(info.getSkuName());
                    orderDetails.add(orderDetail);
                    //将要从购物车中删除的商品信息放入delList集合中
                    delList.add(info.getId());
                }
            }
            orderInfoForDb.setOrderDetailList(orderDetails);
            //将订单保存到数据库中
            orderService.saveOrder(orderInfoForDb);
            // 删除购物车数据
            orderService.removeCheckedCart(delList);
            //刷新购物车缓存
            cartService.flushCache(userId);
            // 重定向到支付页面
            return "redirect:http://payment.gmall.com:8090/index?outTradeNo="+orderInfoForDb.getOutTradeNo()+"&totalAmount="+getCartSum(cartListByUserId)+"&nickName="+nickName;
//            return "payTest";
        }else{
            return "tradeFail";
        }

    }
    //计算商品总额
    private BigDecimal getCartSum(List<CartInfo> cartInfos) {
        BigDecimal totalPrice = new BigDecimal(0);
        //计算好购物车中商品的总价格
        for (CartInfo cartInfo : cartInfos) {
            //被选中的商品才计算价格
            if (cartInfo.getIsChecked().equals("1")) {
                totalPrice = totalPrice.add(cartInfo.getCartPrice());
            }
        }
        return totalPrice;
    }
}
