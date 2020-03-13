package com.spider.amazon.service;

import com.spider.amazon.model.SkuInfoNewDO;

import java.util.List;

/**
 * @ClassName ISkuInfoService
 * @Description SKU信息服务类
 */
public interface ISkuInfoService {

    /**
     * 获取所有SKU信息
     * @return
     */
    public List<SkuInfoNewDO> getAllSkuList() ;


    /**
     * 从复制表入库商品通用信息表
     * @return
     */
    public int intoSkuCommonInfoByCopy();
}
