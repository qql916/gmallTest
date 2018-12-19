package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;

import com.atguigu.gmall.annotations.LoginRequire;
import com.atguigu.gmall.user.bean.CartInfo;
import com.atguigu.gmall.user.bean.SkuInfo;
import com.atguigu.gmall.user.service.CartService;
import com.atguigu.gmall.user.service.SkuService;
import com.atguigu.gmall.user.utils.Acconst;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class cartController {

    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;


    //加入购物车
    @LoginRequire(isNeededSuccess = false)//不一定要登录
    @RequestMapping("addToCart")
    public String addToCart(int num, String skuId, HttpServletRequest request, HttpServletResponse response, RedirectAttributes model) {
        //购物车添加逻辑
        SkuInfo skuInfo = skuService.getSkuInfoBySkuId(skuId);
        CartInfo cartInfo = new CartInfo();
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setSkuId(skuInfo.getId());
        cartInfo.setSkuName(skuInfo.getSkuName());
        cartInfo.setSkuPrice(skuInfo.getPrice());
        cartInfo.setIsChecked("1");
        cartInfo.setCartPrice(skuInfo.getPrice());
        cartInfo.setSkuNum(num);
        //用户id
        List<CartInfo> cartInfos = new ArrayList<>();
        String userId = (String)request.getAttribute("userId");
        //判断用户是否登录
        //如果用户已经登陆
        if (StringUtils.isNotBlank(userId)) {
            //从数据库中查询cartInfo信息
            cartInfo.setUserId(userId);
            CartInfo cartInfoDb = cartService.getCartExists(cartInfo);
            //如果数据库中没有就执行插入操作
            if (cartInfoDb == null) {
                cartService.addCartInfo(cartInfo);
            } else {
                //如果数据库中有信息就执行更新cartInfoDb操作
                //对查询出来的cartInfoDb更新数量和总价
                cartInfoDb.setSkuNum(cartInfoDb.getSkuNum() + num);
                cartInfoDb.setCartPrice(cartInfoDb.getSkuPrice().multiply(new BigDecimal(cartInfoDb.getSkuNum())));
                cartService.updateCartInfo(cartInfoDb);
            }
            //同步购物车缓存
            cartService.flushCache(userId);
        } else {
            //如果用户没有登录
            //从cookie中获取购物车集合信息
            String cartListCookie = CookieUtil.getCookieValue(request, Acconst.CART_COOKIE_NAME, true);
            //如果没有cartListCookie购物车集合信息就购物车信息添加到cookie中
            if (StringUtils.isBlank(cartListCookie)) {
                cartInfos.add(cartInfo);
            } else {
                //有就执行更新
                //从cookie中取出购物车数据，然后将json 数据转换成list集合
                cartInfos = JSON.parseArray(cartListCookie, CartInfo.class);
                //不相同就添加到cookie中的cartInfos 集合，
                boolean b = if_new_cart(cartInfos, cartInfo);
                //当b=true时，执行添加
                if (b) {
                    cartInfos.add(cartInfo);
                } else {
                    for (CartInfo cookieInfo : cartInfos) {//cookieInfo是购物车里原有的商品数据
                        //先判断要添加的商品信息是否和取出的购物车中的商品信息相同，相同就更新商品数量和总价格
                        if (cookieInfo.getSkuId().equals(cartInfo.getSkuId())) {
                            cookieInfo.setSkuNum(cookieInfo.getSkuNum() + num);
                            cookieInfo.setCartPrice(cookieInfo.getSkuPrice().multiply(new BigDecimal(cookieInfo.getSkuNum())));
                        }
                    }
                }
            }
            //购物车集合信息放进cookie中
            CookieUtil.setCookie(request, response, Acconst.CART_COOKIE_NAME, JSON.toJSONString(cartInfos), Acconst.COOKIE_MAX_AGE, true);
        }
//        这种方法是隐藏了参数，链接地址上不直接暴露，但是能且只能在重定向的 “页面” 获取param参数值。其原理就是放到session中，session在跳到页面后马上移除对象。
        model.addFlashAttribute("skuInfo", skuInfo);
        model.addFlashAttribute("num", num);
        return "redirect:/addSuccess";
    }

    //返回商品添加成功页面
    @LoginRequire(isNeededSuccess = false)//不一定要登录
    @RequestMapping("addSuccess")
    public String addSuccess(@ModelAttribute("skuInfo") SkuInfo skuInfo, @ModelAttribute(value = "num") String num, Model model) {
        model.addAttribute("skuInfo", skuInfo);
        model.addAttribute("num", num);
        return "success";
    }

    //判断是否添加新的购物车信息
    private boolean if_new_cart(List<CartInfo> cartInfos, CartInfo cartInfo) {
        boolean b = true;
        for (CartInfo info : cartInfos) {
            if (info.getSkuId().equals(cartInfo.getSkuId())) {
                b = false;
            }
        }
        return b;

    }

    //显示购物车列表
    @LoginRequire(isNeededSuccess = false)//不一定要登录
    @RequestMapping("cartList")
    public String cartList(HttpServletRequest request, ModelMap map) {
        //判断用户是否登录
        String userId = (String)request.getAttribute("userId");
        List<CartInfo> cartInfos = new ArrayList<>();
        if (StringUtils.isBlank(userId)) {
            //如果用户没有登录就取出cookie中的数据
            String cartListCookie = CookieUtil.getCookieValue(request, Acconst.CART_COOKIE_NAME, true);
            if (StringUtils.isNotBlank(cartListCookie)) {
                //将json数据转换成cartInfo类信息集合
                cartInfos = JSON.parseArray(cartListCookie, CartInfo.class);
            }
        } else {
            //如果用户登录了就取缓存中的数据
            cartInfos = cartService.getCartListFromCache(userId);
        }

        //将数据存入到Modelmap中返回给页面
        map.put("cartList", cartInfos);

        //计算好购物车的价格总数
        BigDecimal totalPrice = getCartSum(cartInfos);
        map.put("totalPrice", totalPrice);
        return "cartList";
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

    //选中状态的变更
    @LoginRequire(isNeededSuccess = false)//不一定要登录
    @RequestMapping("checkCart")
    public String checkCart(HttpServletRequest request, HttpServletResponse response, ModelMap map) {
//        同样这里要区分，用户登录和未登录状态。
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");
        List<CartInfo> cartInfos = new ArrayList<>();
        String userId =(String)request.getAttribute("userId");
        if (StringUtils.isNotBlank(userId)) {
//            如果登录，更新数据库中商品的选中状态，
            CartInfo cartInfo = new CartInfo();
            //通过userId和skuId从数据库中查询旧的cartInfo信息
            cartInfo.setUserId(userId);
            cartInfo.setSkuId(skuId);
            CartInfo cartInfoDb = cartService.getCartExists(cartInfo);
            //修改选中的状态，更新数据库
            cartInfoDb.setIsChecked(isChecked);
            cartService.updateCartInfo(cartInfoDb);
            //刷新缓存中的数据，
            cartService.flushCache(userId);
            //从缓存取出新的购物车商品信息
            cartInfos = cartService.getCartListFromCache(userId);
        } else {
            //        如果未登录，修改cookie中的数据
            String cartCookieList = CookieUtil.getCookieValue(request, Acconst.CART_COOKIE_NAME, true);
            //将cartCookieListJSON数据转成cartInfo信息集合
            cartInfos = JSON.parseArray(cartCookieList, CartInfo.class);
            //遍历集合，当skuId相同时，修改cartInfo选中状态
            for (CartInfo cartInfo : cartInfos) {
                if (skuId.equals(cartInfo.getSkuId())) {
                    cartInfo.setIsChecked(isChecked);
                }
            }
            CookieUtil.setCookie(request, response, Acconst.CART_COOKIE_NAME, JSON.toJSONString(cartInfos), Acconst.COOKIE_MAX_AGE, true);

//
        }
        map.put("cartList", cartInfos);
        BigDecimal totalPrice = getCartSum(cartInfos);

        map.put("totalPrice", totalPrice);
        return "cartListInner";
    }

    //删除单个商品信息
    @LoginRequire(isNeededSuccess = false)//不一定要登录
    @RequestMapping("delCartInfoBySkuId")
    public String delCartInfoBySkuId(String skuId,ModelMap map,HttpServletRequest request,HttpServletResponse response){
        String userId = (String)request.getAttribute("userId");
        List<CartInfo> cartInfos = new ArrayList<>();
        //判断用户是否登录了
        if (StringUtils.isNotBlank(userId)){
            //从数据库中删除相关商品信息
            cartService.removeCartInfoBySkuId(userId,skuId);
            // 并且刷新缓存
            cartService.flushCache(userId);
            cartInfos = cartService.getCartListFromCache(userId);
        }else {
            //从cookie中取出所有的商品数据
            String cartListCookie = CookieUtil.getCookieValue(request, Acconst.CART_COOKIE_NAME, true);
            cartInfos = JSON.parseArray(cartListCookie, CartInfo.class);

            //再将skuId的不相等于传入的skuId的商品数据保存到cookie中
            for (CartInfo cartInfo : cartInfos) {
                //skuId相等时，将商品删除
                if (cartInfo.getSkuId().equals(skuId)){
                    cartInfos.remove(cartInfo);
                }
               break;
            }
            //先将cookie的值设为空
            CookieUtil.deleteCookie(request,response,"cartListCookie");
            CookieUtil.setCookie(request,response,Acconst.CART_COOKIE_NAME,JSON.toJSONString(cartInfos), Acconst.COOKIE_MAX_AGE,true);
        }
        map.put("cartList", cartInfos);
        BigDecimal totalPrice = getCartSum(cartInfos);

        map.put("totalPrice", totalPrice);
        return "cartListInner";
    }




}
