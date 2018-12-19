package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.CartInfo;

import java.util.List;

public interface CartService {
    CartInfo getCartExists(CartInfo cartInfo);

    void addCartInfo(CartInfo cartInfo);

    void updateCartInfo(CartInfo cartInfoDb);

    void flushCache(String userId);

    List<CartInfo> getCartListFromCache(String userId);

    void removeCartInfoBySkuId(String userId, String skuId);

    void mergCart(List<CartInfo> cartInfos, String userId);

    List<CartInfo> getCartListByUserId(String userId);

//    void setCheckedCart(String userId, String skuId, String isChecked);
}
