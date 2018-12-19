package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.user.bean.BaseAttrInfo;
import com.atguigu.gmall.user.bean.BaseAttrValue;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrValueMapper extends Mapper<BaseAttrValue>{
    List<BaseAttrInfo> selectAttrListByValueIds(@Param("valueIdStr") String valueIdStr);
}
