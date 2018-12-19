package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.SkuImage;
import com.atguigu.gmall.user.bean.SkuInfo;
import com.atguigu.gmall.user.bean.SkuLsInfo;

import java.util.List;

public interface SkuService {

    List<SkuInfo> getSkuInfoListBySpu(String spuId);


    Integer saveSku(SkuInfo skuInfo);

    SkuInfo getSkuInfoBySkuId(String skuId);

    List<SkuInfo> skuSaleAttrValueListBySpu(String spuId);

    Integer removeSkuInfoBySkuId(String skuId);

    List<SkuInfo> getMySkuInfo(String s);
}
