package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.user.bean.*;

import com.atguigu.gmall.user.service.BaseAttrInfoService;
import com.atguigu.gmall.user.service.SkuService;
import com.atguigu.gmall.user.service.SpuService;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class skuController {

    @Reference
    SkuService skuService;

    @Reference
    SpuService spuService;

    @Reference
    BaseAttrInfoService baseAttrInfoService;

    @ResponseBody
    @RequestMapping("skuInfoListBySpu")
    public List<SkuInfo> skuInfoListBySpu(String spuId){
        List<SkuInfo> skuInfos =  skuService.getSkuInfoListBySpu(spuId);
        return skuInfos;
    }
    @ResponseBody
    @RequestMapping("spuSaleAttrList")
    public List<SpuSaleAttr> spuSaleAttrList(String spuId){

        List<SpuSaleAttr> spuSaleAttrList = spuService.getSpuSaleAttrList(spuId);
        return spuSaleAttrList;
    }

    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){

        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoService.getAttrInfoList(catalog3Id);
        return baseAttrInfos;
    }


    @ResponseBody
    @RequestMapping("saveSku")
    public String saveSku(SkuInfo skuInfo){
        Integer integer = skuService.saveSku(skuInfo);
        if (integer>0){
            return "SUCCESS";
        }else {
            return "FAILED";
        }

    }

    @ResponseBody
    @RequestMapping("removeSkuInfoBySkuId")
    public String removeSkuInfoBySkuId(String skuId){
        Integer integer = skuService.removeSkuInfoBySkuId(skuId);
        if (integer>0){
            return "SUCCESS";
        }else {
            return "FAILED";
        }
    }
}
