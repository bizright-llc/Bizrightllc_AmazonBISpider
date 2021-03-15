package com.spider.amazon.service;

/**
 * @ClassName ISpringBatchCallService
 * @Description 主动发起批处理服务
 */
public interface ISpringBatchCallService {

    /**
     * Vc销量报表数据处理
     * @return
     */
    public void callVcSalesReportDataDeal();


    /**
     * Vc库存报表数据处理
     * @return
     */
    public void callVcInventoryReportDataDeal();


    /**
     * Sc Buy Box报表数据处理
     * @return
     */
    public void callScBuyBoxReportDataDeal();

    public void callScFbaFeeReportDataDeal();
}
