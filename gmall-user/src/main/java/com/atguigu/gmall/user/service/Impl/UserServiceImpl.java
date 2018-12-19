package com.atguigu.gmall.user.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.user.bean.UserAddress;
import com.atguigu.gmall.user.bean.UserInfo;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserService;
import com.atguigu.gmall.user.utils.Acconst;
import com.atguigu.gmall.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.AbstractCachingConfiguration;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;


import java.util.List;
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    UserAddressMapper userAddressMapper;

    @Override
    public List<UserInfo> UserList() {
        List<UserInfo> userInfos = userInfoMapper.selectAll();
        return userInfos;
    }

    @Override
    public List<UserInfo> getUserListWithAddress() {
        List<UserInfo> userInfoList = userInfoMapper.selectAllUserAndAddress();
        return userInfoList;
    }

    @Override
    public Integer saveUser(UserInfo userInfo) {
        int insert = userInfoMapper.insert(userInfo);
        return  insert;
    }



    @Override
    public Integer deleteUser(String userId) {
        int i = userInfoMapper.deleteByPrimaryKey(userId);
        return i;
    }

    @Override
    public Integer updateUserInfo(UserInfo userInfo) {
        int i = userInfoMapper.updateByPrimaryKeySelective(userInfo);
        return i;

    }

    @Override
    public UserInfo selectUserInfo(String userId) {

        UserInfo userInfo = new UserInfo();

        userInfo.setId(userId);
//        UserInfo userInfo2 = userInfoMapper.selectByPrimaryKey(userInfo);
        UserInfo userInfo2 = userInfoMapper.selectOne(userInfo);
        return userInfo2;
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        Example example = new Example(UserInfo.class);
        example.createCriteria().andEqualTo("loginName",userInfo.getLoginName()).andEqualTo("passwd",userInfo.getPasswd());
        UserInfo userInfoDb = userInfoMapper.selectOneByExample(example);
        if (userInfoDb!=null){
            //生成用户信息的key
            String userInfoKey = Acconst.USER_KEY_PREFIX+userInfoDb.getId()+Acconst.INFO_KEY_SUFFIX;
            //存入缓存
            Jedis jedis = redisUtil.getJedis();
            String userInfoJson = JSON.toJSONString(userInfoDb);
            jedis.setex(userInfoKey,60*60*24,userInfoJson);
            return userInfoDb;
        }else{
            return null;
        }

    }

    @Override
    public List<UserAddress> getUserAddressListById(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        List<UserAddress> addressList =  userAddressMapper.select(userAddress);
        return addressList;
    }

    @Override
    public UserAddress getUserAddressByAddressId(String addressId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setId(addressId);
        UserAddress address = userAddressMapper.selectOne(userAddress);
        return address;
    }


}
