package com.spider.amazon.service;

/**
 * @ClassName IFbaInventoryReportDealService
 * @Description FBA Inventory下载报表处理信息服务
 */
public interface FbaInventoryReportDealService {

    public static String COMPLETE_MARK = "PROCESSED";

    /**
     * 获取BOP的InventoryDataDailySnapShot信息
     * @return
     */
    public void dealFbaInventoryReport(String fileName,String filePath,int offerSetDay);

    /**
     * 获取BOP的InventoryDataDailySnapShot信息
     *
     * Deal with the file download from Manage FBA Inventory data
     * It's near real time data
     *
     * @return
     */
    public void dealFbaInventoryReport(String fileName,String filePath);

}
