package com.atguigu.gmall.manage.service.Impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.manage.Acconst.Acconst;
import com.atguigu.gmall.manage.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.manage.mapper.SkuImageMapper;
import com.atguigu.gmall.manage.mapper.SkuInfoMapper;
import com.atguigu.gmall.manage.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.user.bean.*;
import com.atguigu.gmall.user.service.ListService;
import com.atguigu.gmall.user.service.SkuService;
import com.atguigu.gmall.utils.RedisUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    RedisUtil redisUtil;

    @Reference
    ListService listService;

    @Override
    public List<SkuInfo> getSkuInfoListBySpu(String spuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setSpuId(spuId);
        List<SkuInfo> skuInfos = skuInfoMapper.select(skuInfo);
        return skuInfos;
    }

    @Override
    public Integer saveSku(SkuInfo skuInfo) {
        //skuInfo保存到skuInfo表中
        int i = skuInfoMapper.insertSelective(skuInfo);
        String skuId = skuInfo.getId();
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        //将skuAttrValueList中的信息保存到sku_attr_value表中
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
           skuAttrValue.setSkuId(skuId);
           skuAttrValueMapper.insertSelective(skuAttrValue);
        }
        //将skuImageList中的信息保存到sku_image表中
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuId);
            skuImageMapper.insertSelective(skuImage);
        }
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        };

        sendSkuInfoToList(skuInfo);
        return i;

    }
    //保存到elstaicsearch
    public void sendSkuInfoToList(SkuInfo skuInfo){
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        listService.saveSkuInfo(skuLsInfo);
    }
    //从缓存中读取skuInfo数据
    @Override
    public SkuInfo getSkuInfoBySkuId(String skuId){
        SkuInfo skuInfo= null;
        //查询缓存
        Jedis jedis = redisUtil.getJedis();
//        cacheJson为要从缓存中取的数据
        String cacheJson = jedis.get(Acconst.SKU_KEY_PREFIX + skuId + Acconst.SKU_KEY_SUFFIX);
            if(StringUtils.isBlank(cacheJson)){
                //分布式缓存锁服务器取锁
                String OK = jedis.set(Acconst.SKU_KEY_PREFIX +skuId+":lock","1","nx","px",10000);
                //如果取得了分布式缓存锁
                if(StringUtils.isNotBlank(OK)){
                    //在缓存中查询未果则查询数据库
                  skuInfo = getSkuInfoBySkuIdFromDB(skuId);
                  //将数据同步到缓存中
                  if (skuInfo!=null){
                      jedis.set(Acconst.SKU_KEY_PREFIX + skuId + Acconst.SKU_KEY_SUFFIX, JSON.toJSONString(skuInfo));
                      //释放锁
                      jedis.del(Acconst.SKU_KEY_PREFIX+skuId+":lock");
                  }else{
                      // 在缓存中加入有超时时间的空值

                  }
                }else {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //3秒以后开始自旋
                    return getSkuInfoBySkuIdFromDB(skuId);
                }

            }else {
                skuInfo = JSON.parseObject(cacheJson, SkuInfo.class);
            }
        jedis.close();
        return skuInfo;
    }
    //从数据库中获取skuInfo数据
    public SkuInfo getSkuInfoBySkuIdFromDB(String skuId) {

        //查询skuInfo
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        //查询相关图片集合
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImages = skuImageMapper.select(skuImage);
        if (skuImages!=null&&skuImages.size()>0){
            skuInfo.setSkuImageList(skuImages);
        }
        return skuInfo;
    }


    @Override
    public List<SkuInfo> skuSaleAttrValueListBySpu(String spuId) {
        List<SkuInfo>  skuInfos = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
        return skuInfos;
    }

    @Override
    public Integer removeSkuInfoBySkuId(String skuId){
        //先查询出sku_attr_value表和sku_sale_attr_value表中相关的信息一起删除
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValues = skuAttrValueMapper.select(skuAttrValue);
        if (skuAttrValues!=null&&skuAttrValues.size()>0){
            for (SkuAttrValue attrValue : skuAttrValues) {
                skuAttrValueMapper.delete(attrValue);
            }
        }
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuId);
        List<SkuSaleAttrValue> skuSaleAttrValues = skuSaleAttrValueMapper.select(skuSaleAttrValue);
        if (skuSaleAttrValues!=null&&skuSaleAttrValues.size()>0){
            for (SkuSaleAttrValue saleAttrValue : skuSaleAttrValues) {
                skuSaleAttrValueMapper.delete(saleAttrValue);
            }
        }
        int i = skuInfoMapper.deleteByPrimaryKey(skuId);
        return i;
    }

    @Override
    public List<SkuInfo> getMySkuInfo(String s) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setCatalog3Id(s);
        List<SkuInfo> skuInfos = skuInfoMapper.select(skuInfo);
       //查询sku的属性属性值
        for (SkuInfo info : skuInfos) {
            String skuId = info.getId();
            SkuAttrValue skuAttrValue = new SkuAttrValue();
            skuAttrValue.setSkuId(skuId);
            List<SkuAttrValue> skuAttrValues = skuAttrValueMapper.select(skuAttrValue);
            info.setSkuAttrValueList(skuAttrValues);
        }
        return skuInfos;
    }

}
