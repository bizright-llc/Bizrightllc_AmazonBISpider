package com.spider.amazon.service;

import java.util.Map;

/**
 * @ClassName IAmzVcPromotionService
 * @Description Amazon Vc Promotions服务
 */
public interface IAmzVcPromotionService {

    /**
     * 获取VcPromotions服务入库
     */
    public void scrapyPromotionInfo(Map<String, Object> params);


}
