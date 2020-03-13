package com.spider.amazon.service;

import java.util.Map;

/**
 * @ClassName IAmzVcHttpService
 * @Description Amazon VC Http请求服务
 */
public interface IAmzVcHttpService {

    /**
     * 请求VC Promotion列表数据
     */
    public String callVcPromotionsListInfo(Map<String,Object> params);


}
