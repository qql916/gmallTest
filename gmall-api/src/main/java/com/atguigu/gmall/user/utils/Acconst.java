package com.atguigu.gmall.user.utils;

public class Acconst {

    public static final String USER_KEY_PREFIX = "user:";//缓存中有关用户key的前缀
    public static final String CART_KEY_SUFFIX = ":cart";//缓存中有关购物车的key的后缀
    public static final String CART_CHECK_SUFFIX = ":cartChecked";//缓存中有关商品选中状态的key的后缀
    public static final int COOKIE_MAX_AGE = 60 * 60 * 24;//cookie过期时间
    public static final String CART_COOKIE_NAME = "cartListCookie";//购物车cookie的名字
    public static final String INFO_KEY_SUFFIX = ":info";
    public static final String SIGN_KEY = "user0725JavaEE";
    //缓存中用户交易码key后缀
    public static final String TRADE_CODE = ":trade";
    public static int TRADE_CODE_TIME = 60*30;//交易码过期时间半个钟
    public static String TRADE_NO_PREFIX = "atguigugmall";//订单单号前缀
}
