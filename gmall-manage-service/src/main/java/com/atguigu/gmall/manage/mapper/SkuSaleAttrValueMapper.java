package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.user.bean.SkuInfo;
import com.atguigu.gmall.user.bean.SkuSaleAttrValue;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    List<SkuInfo> selectSkuSaleAttrValueListBySpu(String spuId);
}
