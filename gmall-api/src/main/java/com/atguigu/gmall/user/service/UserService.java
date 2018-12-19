package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.UserAddress;
import com.atguigu.gmall.user.bean.UserInfo;

import java.util.List;

public interface UserService {
    List<UserInfo> UserList();

    List<UserInfo> getUserListWithAddress();

    Integer saveUser(UserInfo userInfo);



    Integer deleteUser(String userId);

    Integer updateUserInfo(UserInfo userInfo);

    UserInfo selectUserInfo(String userId);
    //根据传入的用户信息查询相关用户
    UserInfo login(UserInfo userInfo);

    List<UserAddress> getUserAddressListById(String userId);

    UserAddress getUserAddressByAddressId(String addressId);
}
