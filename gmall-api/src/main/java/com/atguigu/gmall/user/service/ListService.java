package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.SkuLsInfo;
import com.atguigu.gmall.user.bean.SkuLsParam;
import com.atguigu.gmall.user.bean.SkuLsResult;

import java.util.List;

public interface ListService {
    public void saveSkuInfo(SkuLsInfo skuLsInfo);
    public List<SkuLsInfo> search(SkuLsParam skuLsParam);
//    public SkuLsResult search(SkuLsParam skuLsParam);
}
