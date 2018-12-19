package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.user.bean.*;
import com.atguigu.gmall.user.service.BaseAttrInfoService;
import com.atguigu.gmall.user.service.ListService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class listController {

    @Reference
    ListService listService;
    @Reference
    BaseAttrInfoService baseAttrInfoService;

    @RequestMapping("index")
    public String toIndexPage() {
        return "index";
    }

    @RequestMapping("list.html")
    public String toListPage(SkuLsParam skuLsParam, ModelMap map) {
        //根据参数返回sku列表
        List<SkuLsInfo> skuLsInfos = listService.search(skuLsParam);
        map.put("skuLsInfoList", skuLsInfos);
//        List<SkuLsInfo> skuLsInfos = listService.search(skuLsParam).getSkuLsInfoList();
        //sku列表结果中包含的属性列表
        Set<String> valueIds = new HashSet<>();
        for (SkuLsInfo skuLsInfo : skuLsInfos) {
            List<SkuLsAttrValue> skuAttrValueList = skuLsInfo.getSkuAttrValueList();
            for (SkuLsAttrValue skuLsAttrValue : skuAttrValueList) {
                String valueId = skuLsAttrValue.getValueId();
                valueIds.add(valueId);
            }
        }
        // 根据sku列表中的属性值查询出的属性列表集合
        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoService.getAttrListByValueIds(valueIds);

        //删除已被选择的属性值的属性列表同时生成面包屑
        //先获取已经被选中的属性值列表数组
        String[] delValueIds = skuLsParam.getValueId();
        if (delValueIds != null && delValueIds.length > 0) {
            //创建面包屑
            List<Crumb> crumbs = new ArrayList<>();
            for (String delValueId : delValueIds) {
                Crumb crumb = new Crumb();
                //对属性进行迭代
                Iterator<BaseAttrInfo> iterator = baseAttrInfos.iterator();
                //当还有下一个值的时候继续执行
                while (iterator.hasNext()) {
                    //获取属性信息
                    BaseAttrInfo baseAttrInfo = iterator.next();
                    //获取属性值
                    List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                    for (BaseAttrValue baseAttrValue : attrValueList) {
                        String attrValueId = baseAttrValue.getId();
                        //如果属性值Id 等于正好要删除的属性Id就将属性从baseAttrInfos集合删除，并将生成相应的面包屑
                        if (attrValueId.equals(delValueId)) {
                            //获取请求路径参数
                            String urlParam = getMyUrlParam(skuLsParam, delValueId);
                            crumb.setUrlParam(urlParam);
                            crumb.setValueName(baseAttrValue.getValueName());
                            iterator.remove();
                        }

                    }
                }

                crumbs.add(crumb);
            }
            map.put("attrValueSelectedList", crumbs);
        }
        //属性集合
        map.put("attrList", baseAttrInfos);
//        上一次请求的参数列表
        String urlParam = getMyUrlParam(skuLsParam);
        map.put("urlParam", urlParam);
        return "list";
    }


    private String getMyUrlParam(SkuLsParam skuLsParam,String...delValueIds) {
        String urlParam = "";
        //获取skuLsParam中的参数
        String keyword = skuLsParam.getKeyword();
        String catalog3Id = skuLsParam.getCatalog3Id();
        String[] valueIds = skuLsParam.getValueId();
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }

//
           if (valueIds != null) {

               for (String valueId : valueIds) {
//
                       if (delValueIds.length>0&&delValueIds!=null){
                           //遍历数组，比较delValueId和valueId是否相同
                           for (String delValueId : delValueIds) {
                               if (!delValueId.equals(valueId)){
                                   urlParam = urlParam + "&valueId=" + valueId;
                               }
                           }
                       }else {
                           urlParam = urlParam + "&valueId=" + valueId;
                       }
//                   urlParam = urlParam + "&valueId=" + valueId;
               }
           }
//       }
//

        return urlParam;
    }
}
