package com.atguigu.gmall.manage.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.user.bean.*;
import com.atguigu.gmall.user.service.ManageService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class ManageServiceImpl implements ManageService{

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;


    @Override
    public List<BaseCatalog1> getCatalog1() {
        List<BaseCatalog1> catalog1s = baseCatalog1Mapper.selectAll();
        return catalog1s;
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        List<BaseCatalog2> catalog2s = baseCatalog2Mapper.select(baseCatalog2);
        return catalog2s;
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> catalog3s = baseCatalog3Mapper.select(baseCatalog3);
        return catalog3s;
    }


}
