package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.atguigu.gmall.user.bean.BaseCatalog1;
import com.atguigu.gmall.user.bean.BaseCatalog2;
import com.atguigu.gmall.user.bean.BaseCatalog3;
import com.atguigu.gmall.user.service.ManageService;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


import java.util.List;

@Controller
public class managerController {

    @Reference
    private ManageService manageService;

    @RequestMapping("index")
    public String toIndexPage() {
        return "index";
    }


   @ResponseBody
    @RequestMapping("getCatalog1")
    public List<BaseCatalog1> getCatalog1() {

        List<BaseCatalog1> catalog1s = manageService.getCatalog1();
        return catalog1s;
    }
    @ResponseBody
    @RequestMapping("getCatalog2")
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {

        List<BaseCatalog2> catalog2s = manageService.getCatalog2(catalog1Id);
        return catalog2s;
    }
    @ResponseBody
    @RequestMapping("getCatalog3")
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {

        List<BaseCatalog3> catalog3s = manageService.getCatalog3(catalog2Id);
        return catalog3s;
    }
}
