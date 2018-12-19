package com.atguigu.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.user.bean.UserInfo;
import com.atguigu.gmall.user.service.UserService;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Reference
    private UserService userService;

    @RequestMapping("getUserList")
    public List<UserInfo> getUserList(){
       List<UserInfo> userInfos = userService.UserList();
        return userInfos;
    }

    @RequestMapping("getUserListWithAddress")
    public List<UserInfo> getUserListWithAddress(){
        List<UserInfo> userInfoList = userService.getUserListWithAddress();
        return userInfoList;
    }
    @RequestMapping("saveUser")
    public Integer saveUser(){
        UserInfo user = new UserInfo(null,"dyl","dyl123","123456","董又霖","13245654126","12345684@qq.com",null,"普通");

         int count = userService.saveUser(user);
        return count;
    }



    @RequestMapping("getUserInfo/{userId}")
    public  UserInfo getUserInfo(@PathVariable("userId") String userId){

        System.out.println(userId);

        UserInfo userInfo = userService.selectUserInfo(userId);
        System.out.println(userInfo);
        return userInfo;
    }

    @RequestMapping("deleteUserInfoById/{userId}")
    public Integer deleteUserInfoById(@PathVariable("userId") String userId){
        Integer integer = userService.deleteUser(userId);
        return  integer;
    }

    @RequestMapping("updateUserInfo")
    public Integer updateUserInfo(){
        UserInfo userInfo = new UserInfo("2","hxh","hxh123","123456","黄明昊","13245654126","12345684@qq.com",null,"普通");

        Integer integer = userService.updateUserInfo(userInfo);

        return integer;
    }

}
