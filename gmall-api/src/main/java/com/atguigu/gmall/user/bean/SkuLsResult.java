package com.atguigu.gmall.user.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @param
 * @return
 */
public class SkuLsResult implements Serializable{

    List<SkuLsInfo> skuLsInfoList;

    Long Total;

    long totalPages;

    List<String> valueIdList;

    public List<SkuLsInfo> getSkuLsInfoList() {
        return skuLsInfoList;
    }

    public void setSkuLsInfoList(List<SkuLsInfo> skuLsInfoList) {
        this.skuLsInfoList = skuLsInfoList;
    }

    public Long getTotal() {
        return Total;
    }

    public void setTotal(Long total) {
        this.Total = total;
    }

    public List<String> getValueIdList() {
        return valueIdList;
    }

    public void setValueIdList(List<String> valueIdList) {
        this.valueIdList = valueIdList;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(long totalPages) {
        this.totalPages = totalPages;
    }
}
