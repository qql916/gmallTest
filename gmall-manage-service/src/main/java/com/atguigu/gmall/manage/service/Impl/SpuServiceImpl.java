package com.atguigu.gmall.manage.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.user.bean.*;
import com.atguigu.gmall.user.service.SpuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    SpuImageMapper spuImageMapper;
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Override
    public List<SpuInfo> getSpuInfoList(String catalog3Id) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        List<SpuInfo> spuInfos  = spuInfoMapper.select(spuInfo);
        return spuInfos;
    }
//获取所有的销售属性
    @Override
    public List<BaseSaleAttr> getBaseSaleAttr() {
        List<BaseSaleAttr> baseSaleAttrs = baseSaleAttrMapper.selectAll();

        return  baseSaleAttrs;
    }

    @Override
    public void saveSpu(SpuInfo spuInfo) {
        //判断是否存在spuId
       String spuIdOld =  spuInfo.getId();
       if (spuIdOld!= null){
           //先刪除原來的信息
           //1.删除原来的图片信息
           SpuImage spuImage = new SpuImage();
           spuImage.setSpuId(spuIdOld);
           spuImageMapper.delete(spuImage);
           //2.删除原来的销售属性值
           SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
           spuSaleAttrValue.setSpuId(spuIdOld);
           spuSaleAttrValueMapper.delete(spuSaleAttrValue);
           //3.删除原来的销售属性
           SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
           spuSaleAttr.setSpuId(spuIdOld);
           spuSaleAttrMapper.delete(spuSaleAttr);
           //4.删除spu信息
           SpuInfo spuInfoOld= new SpuInfo();
           spuInfoOld.setId(spuIdOld);
           spuInfoMapper.delete(spuInfoOld);
       }
        //保存spu信息
        int i = spuInfoMapper.insertSelective(spuInfo);
        System.out.println(i);
        String spuId = spuInfo.getId();


        //保存spu图片信息
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        for (SpuImage spuImage:spuImageList) {
            spuImage.setSpuId(spuId);
            spuImageMapper.insertSelective(spuImage);
        }
        //保存销售属性信息
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        for ( SpuSaleAttr spuSaleAttr :spuSaleAttrList) {
//            销售属性
            spuSaleAttr.setSpuId(spuId);
            spuSaleAttrMapper.insertSelective(spuSaleAttr);
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue spuSaleAttrValue:spuSaleAttrValueList) {
                spuSaleAttrValue.setSpuId(spuId);
                spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
            }
        }

    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuId);
        //获取销售属性
        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.select(spuSaleAttr);
        //获取销售属性值
        for (SpuSaleAttr spuSaleAttr2:spuSaleAttrs) {

            SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
            spuSaleAttrValue.setSpuId(spuId);
            spuSaleAttrValue.setSaleAttrId(spuSaleAttr2.getSaleAttrId());
            //获取到销售属性值集合
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttrValueMapper.select(spuSaleAttrValue);
           //封装到销售属性中
            spuSaleAttr2.setSpuSaleAttrValueList(spuSaleAttrValueList);
        }

        return spuSaleAttrs;
    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        List<SpuImage> spuImages = spuImageMapper.select(spuImage);
        return spuImages;
    }

    @Override
    public List<SpuSaleAttr> SpuSaleAttrList(String spuId, String skuId) {
        Map<String, Object> map = new HashMap<>();
        map.put("skuId",skuId);
        map.put("spuId",spuId);
        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.selectSpuSaleAttrListBySpuId(map);
        return spuSaleAttrs;
    }

    //删除spu信息并同时删除spu下的所有sku信息
    @Override
    public Integer removeSpuInfoBySpuId(String spuId) {
        //1.删除原来的图片信息
        if (!StringUtils.isBlank(spuId)){
            //删除spu信息时要同时删除spu下所有的sku信息
            SkuInfo skuInfo = new SkuInfo();
            skuInfo.setSpuId(spuId);
            //先查询出spuId相同的sku
            List<SkuInfo> skuInfos = skuInfoMapper.select(skuInfo);
            if (skuInfos!=null&&skuInfos.size()>0) {
                for (SkuInfo info : skuInfos) {
                    //删除skuImage表中信息
                    List<SkuImage> skuImageList = info.getSkuImageList();
                    if (skuImageList != null && skuImageList.size() > 0) {
                        for (SkuImage skuImage : skuImageList) {
                            skuImageMapper.delete(skuImage);
                        }
                    }
                    //删除sku销售属性值
                    List<SkuSaleAttrValue> skuSaleAttrValueList = info.getSkuSaleAttrValueList();
                    if (skuSaleAttrValueList != null && skuSaleAttrValueList.size() > 0) {
                        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                            skuSaleAttrValueMapper.delete(skuSaleAttrValue);
                        }
                    }

                    //删除sku——attr_value表相关数据
                    List<SkuAttrValue> skuAttrValueList = info.getSkuAttrValueList();
                    if (skuAttrValueList != null && skuAttrValueList.size() > 0) {
                        for (SkuAttrValue skuAttrValue : skuAttrValueList) {

                            skuAttrValueMapper.delete(skuAttrValue);
                        }
                    }
                    skuInfoMapper.delete(info);
                }
            }
            SpuImage spuImage = new SpuImage();
            spuImage.setSpuId(spuId);
            int deleteSpuImage = spuImageMapper.delete(spuImage);
            //2.删除原来的销售属性值
            SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
            spuSaleAttrValue.setSpuId(spuId);
            spuSaleAttrValueMapper.delete(spuSaleAttrValue);
            //3.删除原来的销售属性
            SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
            spuSaleAttr.setSpuId(spuId);
            spuSaleAttrMapper.delete(spuSaleAttr);
            //.删除spu信息
            SpuInfo spuInfoOld= new SpuInfo();
            spuInfoOld.setId(spuId);
            int delete = spuInfoMapper.delete(spuInfoOld);
            return delete;
        }else {
            return 0;
        }

    }
}
