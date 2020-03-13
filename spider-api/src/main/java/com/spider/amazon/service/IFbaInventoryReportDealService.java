package com.spider.amazon.service;

/**
 * @ClassName IFbaInventoryReportDealService
 * @Description FBA Inventory下载报表处理信息服务
 */
public interface IFbaInventoryReportDealService {

    /**
     * 获取BOP的InventoryDataDailySnapShot信息
     * @return
     */
    public void dealFbaInventoryReport(String fileName,String filePath,int offerSetDay);

}
