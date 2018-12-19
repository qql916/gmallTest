package com.atguigu.gmall.cart.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.user.bean.CartInfo;
import com.atguigu.gmall.user.service.CartService;
import com.atguigu.gmall.user.utils.Acconst;
import com.atguigu.gmall.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public CartInfo getCartExists(CartInfo cartInfo) {

        CartInfo info = new CartInfo();
        info.setUserId(cartInfo.getUserId());
        info.setSkuId(cartInfo.getSkuId());
        CartInfo cartInfoDb = cartInfoMapper.selectOne(info);
        return cartInfoDb;
    }

    @Override
    public void addCartInfo(CartInfo cartInfo) {
        cartInfoMapper.insertSelective(cartInfo);
    }

    @Override
    public void updateCartInfo(CartInfo cartInfoDb) {
        Example e = new Example(CartInfo.class);
        e.createCriteria().andEqualTo("userId", cartInfoDb.getUserId()).andEqualTo("skuId", cartInfoDb.getSkuId());
        cartInfoMapper.updateByExampleSelective(cartInfoDb, e);
    }

    //刷新缓存
    @Override
    public void flushCache(String userId) {
        //根据用户id先从数据库查询出用户的购物车信息
        CartInfo info = new CartInfo();
        info.setUserId(userId);
        List<CartInfo> cartInfos = cartInfoMapper.select(info);
        if (cartInfos!=null&&cartInfos.size()>0){
            //获取jedis连接
            Jedis jedis = redisUtil.getJedis();
            //先将原来的缓存中用户的购物车的key全部删除
            jedis.del(Acconst.USER_KEY_PREFIX + userId + Acconst.CART_KEY_SUFFIX);
            //创建一个map集合存放用户的购物车信息
            Map<String, String> map = new HashMap<>();
            for (CartInfo cartInfo : cartInfos) {
//            field-value  field-->skuId  value--->cartInfo

                map.put(cartInfo.getSkuId(), JSON.toJSONString(cartInfo));

            }
            //创建新的键值对存储用户购物车信息
            jedis.hmset(Acconst.USER_KEY_PREFIX + userId + Acconst.CART_KEY_SUFFIX, map);//同时将多个 field-value (域-值)对设置到哈希表 key 中。
            //关闭连接
            jedis.close();
        }else{
            return;
        }

    }

    //获取缓存中的购物车信息
    @Override
    public List<CartInfo> getCartListFromCache(String userId) {
        //创建一个集合存放cartInfos 信息
        List<CartInfo> cartInfos = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();
        List<String> hvals = jedis.hvals(Acconst.USER_KEY_PREFIX + userId + Acconst.CART_KEY_SUFFIX);//获取哈希表中所有值
        if (hvals.size() > 0 && hvals != null) {
            for (String hval : hvals) {
                CartInfo info = new CartInfo();
                info = JSON.parseObject(hval, CartInfo.class);
                cartInfos.add(info);
            }
        }
        jedis.close();
        return cartInfos;
    }

    @Override
    public void removeCartInfoBySkuId(String userId, String skuId) {
        CartInfo info = new CartInfo();
        info.setSkuId(skuId);
        info.setUserId(userId);
        cartInfoMapper.delete(info);
    }

    //合并购物车
    @Override
    public void mergCart(List<CartInfo> cartInfoCookies, String userId) {
        CartInfo info = new CartInfo();
        info.setUserId(userId);
        List<CartInfo> cartInfoListDb = cartInfoMapper.select(info);
        for (CartInfo cartInfoCookie : cartInfoCookies) {
            //判断cookie中的购物车的商品信息在数据库中的是否存在
            boolean b = if_new_cart(cartInfoListDb, cartInfoCookie);
            if (b) {
                //添加新的商品信息
                cartInfoCookie.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCookie);
            } else {
                //修改商品的数量和总价
                for (CartInfo cartInfo : cartInfoListDb) {
                    if (cartInfo.getSkuId().equals(cartInfoCookie.getSkuId())) {
                        cartInfo.setSkuNum(cartInfo.getSkuNum() + cartInfoCookie.getSkuNum());
                        cartInfo.setCartPrice(cartInfo.getSkuPrice().multiply(new BigDecimal(cartInfo.getSkuNum())));
                        //更新数据库
                        cartInfoMapper.updateByPrimaryKeySelective(cartInfo);
                    }
                }
            }
        }
        //刷新缓存
        flushCache(userId);
    }

    //获取用户所有的购物车商品
    @Override
    public List<CartInfo> getCartListByUserId(String userId) {
        CartInfo info = new CartInfo();
        info.setUserId(userId);
        List<CartInfo> cartInfos = cartInfoMapper.select(info);
        return cartInfos;
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
    //    把对应skuId的购物车的信息从redis中取出来，反序列化，修改isChecked标志。
//    再保存回redis中。
//    同时保存另一个redis的key 专门用来存储用户选中的商品，方便结算页面使用。
//    @Override
//    public void setCheckedCart(String userId, String skuId, String isChecked) {
//        //获取redis连接
//        Jedis jedis = redisUtil.getJedis();
//        //缓存中保存用户购物车的userCartKey
//        String userCartKey = Acconst.USER_KEY_PREFIX+userId+Acconst.CART_KEY_SUFFIX;
//        //通过key 和skuId获取商品Id为skuId的商品数据
//        String cartInfoJson = jedis.hget(userCartKey, skuId);//获取存储在哈希表中指定字段的值。
//        //转换成CartInfo数据
//        CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
//        //将选中状态保存到商品信息中
//        cartInfo.setIsChecked(isChecked);
//        //转换成JSON
//        String newCartInfoJson = JSON.toJSONString(cartInfo);
//        //保存新的商品信息到用户购物车缓存中,
//        jedis.hset(userCartKey,skuId,newCartInfoJson);//将哈希表 key 中的字段 field 的值设为 value
//        //新建购物车中商品选中状态的cartCheckedKey
//        String cartCheckedKey = Acconst.USER_KEY_PREFIX+userId+Acconst.CART_CHECK_SUFFIX;
//        //判断isChecked是否等于1
//        if (isChecked.equals("1")){
//            //等于1就保存cartCheckedKey，skuId，newCartInfoJson到缓存中
//            jedis.hset(cartCheckedKey,skuId,newCartInfoJson);
//            //设置redis key的expire过期时间为用户购物车key的剩余时间
//            jedis.expire(cartCheckedKey,jedis.ttl(userCartKey).intValue());
//        }else {
//            //不等于就删除
//            jedis.del(cartCheckedKey,skuId);
//        }
//    }
}
