package com.atguigu.gmall.util;

public class webAcconstUtil {
    //登录页面
    public final static String LOGIN_URL="http://passport.gmall.com:8086/index";
    //认证接口
    public final static String VERIFY_URL="http://passport.gmall.com:8086/verify";
    //cookie的有效时间：默认给7天
    public final static int cookieMaxAge=7*24*3600;
    public static final String SIGN_KEY = "user0725JavaEE";
    //用户cookie中的token
    public static final String  USER_TOKEN ="userToken";
}
