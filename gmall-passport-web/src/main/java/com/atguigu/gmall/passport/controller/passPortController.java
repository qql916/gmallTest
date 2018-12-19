package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.user.bean.CartInfo;
import com.atguigu.gmall.user.bean.UserInfo;
import com.atguigu.gmall.user.service.CartService;
import com.atguigu.gmall.user.service.UserService;
import com.atguigu.gmall.user.utils.Acconst;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class passPortController {
    @Reference
    UserService userService;
    @Reference
    CartService cartService;

    @RequestMapping("index")
    public String index(String returnUrl, ModelMap modelMap){
        modelMap.addAttribute("returnUrl",returnUrl);
        return "index";
    }
    //认证入口
    @ResponseBody
    @RequestMapping("verify")
    public String verify(HttpServletRequest request,String currentIp,String token){
        //解密
        Map map = JwtUtil.decode(Acconst.SIGN_KEY, token, currentIp);
        if (map!=null){
            return "success";
        }else{
            return "fail";
        }

    }
    //登录操作
    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request, HttpServletResponse response){
//
        String signKey = Acconst.SIGN_KEY;
        //根据收集到的用户信息在数据库查询用户id
        UserInfo userInfoDb = userService.login(userInfo);
        String userId = userInfoDb.getId();
        //如果有id就生成token
        if (StringUtils.isNotBlank(userId)){
            // 通过负载均衡nginx
            String ip = request.getHeader("x-forwarded-for");
            if(StringUtils.isBlank(ip)){
                ip = request.getRemoteAddr();
                if(StringUtils.isBlank(ip)){
                    ip = "127.0.0.1";
                }
            }
            Map<String,String> map = new HashMap<>();
            map.put("userId",userId);
            map.put("nickName",userInfoDb.getNickName());

            String token = JwtUtil.encode(signKey, map, ip);

            //合并购物车
            String cookieValue = CookieUtil.getCookieValue(request, Acconst.CART_COOKIE_NAME, true);
            if (StringUtils.isNotBlank(cookieValue)){
                List<CartInfo> cartInfos = JSON.parseArray(cookieValue, CartInfo.class);
                // 合并当前用户的购物车
                cartService.mergCart(cartInfos,userId);
                //清理cookie
                CookieUtil.deleteCookie(request,response,Acconst.CART_COOKIE_NAME);
            }else {
                cartService.flushCache(userId);
            }
            return token;
        }else{
            //没有id返回null
            return "fail";
        }


    }

}
