package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.BaseSaleAttr;
import com.atguigu.gmall.user.bean.SpuImage;
import com.atguigu.gmall.user.bean.SpuInfo;
import com.atguigu.gmall.user.bean.SpuSaleAttr;

import java.util.List;

public interface SpuService {
    List<SpuInfo> getSpuInfoList(String catalog3Id);

    List<BaseSaleAttr> getBaseSaleAttr();

    void saveSpu(SpuInfo spuInfo);

    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    List<SpuImage> getSpuImageList(String spuId);


    List<SpuSaleAttr> SpuSaleAttrList(String spuId, String skuId);

    Integer removeSpuInfoBySpuId(String spuId);
}
