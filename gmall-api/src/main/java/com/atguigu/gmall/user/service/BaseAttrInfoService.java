package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.BaseAttrInfo;

import java.util.List;
import java.util.Set;

public interface BaseAttrInfoService {
    List<BaseAttrInfo> getAttrInfoList(String catalog3Id);

    List<BaseAttrInfo> getAttrListByValueIds(Set<String> valueIds);
}
