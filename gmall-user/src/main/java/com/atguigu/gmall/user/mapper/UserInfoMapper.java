package com.atguigu.gmall.user.mapper;

import com.atguigu.gmall.user.bean.UserAddress;
import com.atguigu.gmall.user.bean.UserInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserInfoMapper extends Mapper<UserInfo>{

    List<UserInfo> selectAllUserAndAddress();


}
