package com.atguigu.gmall.manage.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.manage.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.manage.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.user.bean.BaseAttrInfo;
import com.atguigu.gmall.user.bean.BaseAttrValue;
import com.atguigu.gmall.user.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {

        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> attrInfoList = baseAttrInfoMapper.select(baseAttrInfo);
        return attrInfoList;

    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);
        return baseAttrValueList;
    }

    @Override
    public void saveAttr(BaseAttrInfo baseAttrInfo) {
        //获取属性的id
        String attrInfoId = baseAttrInfo.getId();
        //判断属性id是否为空，为空则执行保存新增的属性操作，不为空执行更新操作
        if (StringUtils.isBlank(attrInfoId)){
            //保存新的属性
            //防止主键被赋上一个空字符串
            baseAttrInfo.setId(null);

//            String catalog3Id = baseAttrInfo.getCatalog3Id();
//            baseAttrInfo.setCatalog3Id(catalog3Id);
            baseAttrInfoMapper.insertSelective(baseAttrInfo);

        }else{

            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
            //更新属性的属性值
        }
        //先原来的属性值删除
        BaseAttrValue baseAttrValue1 = new BaseAttrValue();
        baseAttrValue1.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue1);
        //保存属性值
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        for (BaseAttrValue baseAttrValue :attrValueList) {
            //防止主键被赋上一个空字符串
            if(baseAttrValue.getId()!=null&&baseAttrValue.getId().length()==0){
                baseAttrValue.setId(null);
            }
            attrInfoId = baseAttrInfo.getId();
            baseAttrValue.setAttrId(attrInfoId);
            baseAttrValueMapper.insertSelective(baseAttrValue);
        }
    }

    @Override
    public Integer removeAttr(BaseAttrInfo baseAttrInfo) {
        String attrInfoId = baseAttrInfo.getId();
        if(StringUtils.isBlank(attrInfoId)){
            return 0;
        }
            //先删除baseInfoValue表里对应的属性值
            //
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrInfoId);
        baseAttrValueMapper.delete(baseAttrValue);
        //再删除baseAttrInfo表中对应的属性
        int delete = baseAttrInfoMapper.delete(baseAttrInfo);
        return  delete;
    }


}
