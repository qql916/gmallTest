package com.atguigu.gmall.manage.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.manage.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.manage.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.user.bean.BaseAttrInfo;
import com.atguigu.gmall.user.bean.BaseAttrValue;
import com.atguigu.gmall.user.service.BaseAttrInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

@Service
public class BaseAttrInfoServiceImpl implements BaseAttrInfoService {

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;
    @Override
    public List<BaseAttrInfo> getAttrInfoList(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        //获取平台属性
        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.select(baseAttrInfo);
        //获取平台属性的属性值
        for (BaseAttrInfo attrInfo : baseAttrInfos) {
            BaseAttrValue baseAttrValue = new BaseAttrValue();
            baseAttrValue.setAttrId(attrInfo.getId());
            List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.select(baseAttrValue);
            attrInfo.setAttrValueList(baseAttrValues);
        }
        return baseAttrInfos;
    }

    @Override
    public List<BaseAttrInfo> getAttrListByValueIds(Set<String> valueIds) {
        String valueIdStr = StringUtils.join(valueIds, ",");
        List<BaseAttrInfo> baseAttrInfos  = baseAttrValueMapper.selectAttrListByValueIds(valueIdStr);
        return baseAttrInfos;
    }
}
