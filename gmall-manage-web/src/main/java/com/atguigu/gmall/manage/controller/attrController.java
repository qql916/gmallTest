package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.user.bean.BaseAttrInfo;
import com.atguigu.gmall.user.bean.BaseAttrValue;
import com.atguigu.gmall.user.bean.BaseCatalog3;
import com.atguigu.gmall.user.service.AttrService;
import com.atguigu.gmall.user.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class attrController {

    @Reference
    private AttrService attrService;

    @RequestMapping("attrListPage")
    public String attrListPage(){
        return "attrListPage";
    }


    @ResponseBody
    @RequestMapping("getAttrList")
    public List<BaseAttrInfo> getAttrList(String catalog3Id){
        List<BaseAttrInfo> attrInfoList = attrService.getAttrList(catalog3Id);
        return  attrInfoList;
    }

    @ResponseBody
    @RequestMapping("getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId){
        List<BaseAttrValue> attrValueList = attrService.getAttrValueList(attrId);
        return attrValueList;
    }

    @ResponseBody
    @RequestMapping("saveAttr")
    public String saveAttr(BaseAttrInfo baseAttrInfo){
        attrService.saveAttr(baseAttrInfo);
        return "SUCCESS";
    }

    @ResponseBody
    @RequestMapping("removeAttr")
    public String removeAttr(BaseAttrInfo baseAttrInfo){
        Integer integer = attrService.removeAttr(baseAttrInfo);
        if (integer>0){
            return "SUCCESS";
        }else {
            return "FAILED";
        }
    }
}
