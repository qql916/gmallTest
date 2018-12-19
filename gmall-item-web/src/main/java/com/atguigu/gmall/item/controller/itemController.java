package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.user.bean.SkuInfo;
import com.atguigu.gmall.user.bean.SkuSaleAttrValue;
import com.atguigu.gmall.user.bean.SpuSaleAttr;
import com.atguigu.gmall.user.service.SkuService;
import com.atguigu.gmall.user.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class itemController {

    @Reference
    SkuService skuService;
    @Reference
    SpuService spuService;
    @RequestMapping("{skuId}.html")
    public String item(@PathVariable("skuId")String skuId, ModelMap map){

        //查询当前sku详情
       SkuInfo skuInfo =  skuService.getSkuInfoBySkuId(skuId);
        map.put("skuInfo",skuInfo);
//      通过skuId获得spuId
        String spuId = skuInfo.getSpuId();
        //根据spuId和skuId联合查询销售属性集合
        List<SpuSaleAttr> spuSaleAttrs = spuService.SpuSaleAttrList(spuId,skuId);
        map.put("spuSaleAttrListCheckBySku",spuSaleAttrs);
        // 根据spuId制作页面销售属性的hash表
        // 销售属性组合：skuId
        //通过spuId 和skuSaleAttrValueId获取skuInfo集合
        List<SkuInfo> skuInfos =skuService.skuSaleAttrValueListBySpu(spuId);
        //创建一个Map集合存放hash表
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        for (SkuInfo info : skuInfos) {
            String skuSaleAttrValueIdsKey = "";
            //获取sku销售属性值集合
            List<SkuSaleAttrValue> skuSaleAttrValueList = info.getSkuSaleAttrValueList();
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                //将销售属性值组装在一起
                skuSaleAttrValueIdsKey=skuSaleAttrValueIdsKey+"|"+skuSaleAttrValue.getSaleAttrValueId();
            }
            String skuIdValue = info.getId();
            //将销售属性值id和skuId一一对应保存在hash表中
            stringStringHashMap.put(skuSaleAttrValueIdsKey,skuIdValue);
        }
        String s = JSON.toJSONString(stringStringHashMap);
        map.put("valuesSkuJson",s);
        return "item";
    }
}
