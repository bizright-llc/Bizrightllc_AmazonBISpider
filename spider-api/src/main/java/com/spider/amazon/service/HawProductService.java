package com.spider.amazon.service;

import java.util.Map;

/**
 * Haw网站产品服务
 */
public interface HawProductService {
    /**
     * 任务对应VendorSku入库
     * @return
     */
    public int taskFileVendorSkuIntoDB(Map<String, Object> paramMap);


}
