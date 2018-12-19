package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.BaseAttrInfo;
import com.atguigu.gmall.user.bean.BaseAttrValue;

import java.util.List;

public interface AttrService {
    List<BaseAttrInfo> getAttrList(String catalog3Id);

    List<BaseAttrValue> getAttrValueList(String attrId);

    void saveAttr(BaseAttrInfo baseAttrInfo);

    Integer removeAttr(BaseAttrInfo baseAttrInfo);
}
