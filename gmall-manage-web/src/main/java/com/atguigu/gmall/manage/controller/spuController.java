package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.manage.utils.GmallUploadUtil;
import com.atguigu.gmall.user.bean.*;
import com.atguigu.gmall.user.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class spuController {

    @Reference
    private SpuService spuService;


    @RequestMapping("spuListPage")
    public String toIndexPage() {
        return "spuListPage";
    }

    @ResponseBody
    @RequestMapping("getSpuList")
    public List<SpuInfo> getSpuList(String catalog3Id){

        List<SpuInfo> spuInfos  = spuService.getSpuInfoList(catalog3Id);
        return  spuInfos;
    }
    //获取销售属性
    @ResponseBody
    @RequestMapping("baseSaleAttrList")
    public List<BaseSaleAttr> baseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrs =  spuService.getBaseSaleAttr();
        return baseSaleAttrs;
    }

    @ResponseBody
    @RequestMapping("saveSpu")
    public String saveSpu(SpuInfo spuInfo){
        spuService.saveSpu(spuInfo);
        return "SUCCESS";
    }

    //文件上传
    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){

        String imgUrl = GmallUploadUtil.uploadImage(multipartFile);
        // 保存spu
        return imgUrl;
    }

    @ResponseBody
    @RequestMapping("getSpuSaleAttrList")
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId){
        List<SpuSaleAttr> spuSaleAttrs = spuService.getSpuSaleAttrList(spuId);
        return spuSaleAttrs;
    }

    @ResponseBody
    @RequestMapping("spuImageList")
    public List<SpuImage> spuImageList(String spuId){
        List<SpuImage> spuImages = spuService.getSpuImageList(spuId);
        return spuImages;
    }
    @ResponseBody
    @RequestMapping("removeSpuInfoBySpuId")
    public String removeSpuInfoBySpuId(String spuId){
        Integer i= spuService.removeSpuInfoBySpuId(spuId);
        if(i>0){
            return "SUCCESS";
        }else{
            return "FAILED";
        }

    }
}
