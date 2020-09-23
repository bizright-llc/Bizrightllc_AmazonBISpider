package com.spider.amazon.remote.api;

/**
 * @ClassName SpiderUrl
 * @Description 爬虫的路径
 */
public class SpiderUrl {
    // 爬虫默认主页
    public static final String SPIDER_INDEX = "https://www.google.com";

    // Amazon主页
    public static final String AMAZON_INDEX = "https://www.amazon.com";

    // SC主页
    public static final String SPIDER_SC_INDEX = "https://sellercentral.amazon.com/";

    // SC Buy Box 报表url
    public static final String SPIDER_SC_BUYBOX = "https://sellercentral.amazon.com/gp/site-metrics/report.html#&cols=/c0/c1/c2/c3/c4/c5/c6/c7/c8/c9/c10/c11/c12/c13/c14/c15&sortColumn=16&filterFromDate={filterFromDate}&filterToDate={filterToDate}&fromDate={fromDate}&toDate={toDate}&reportID=102:DetailSalesTrafficByChildItem&sortIsAscending=0&currentPage=0&dateUnit=1&viewDateUnits=ALL&runDate=";

    // SC Fba Inventory 报表主页
    public static final String SPIDER_SC_FBAINV ="https://sellercentral.amazon.com/gp/ssof/reports/search.html";

    // Haw主页
    public static final String SPIDER_HAW_INDEX = "https://www.hawthornegc.com";

    // IP池主页
    public static final String IP_POOL_INDEX = "https://www.kuaidaili.com";

    /**
     * This page let system add cookies to browser
     */
    // Amazon VC index 頁面
    public static final String AMAZON_VC_INDEX = "https://vendorcentral.amazon.com/404page";

    public static final String AMAZON_VC_DASHBOARD = "https://vendorcentral.amazon.com/analytics/dashboard";

    public static final String AMAZON_VC_ANALYTICS_SALES_DIAGNOSTIC = "https://vendorcentral.amazon.com/analytics/dashboard/salesDiagnostic";

    public static final String AMAZON_VC_ANALYTICS_INVENTORY_HEALTH = "https://vendorcentral.amazon.com/analytics/dashboard/inventoryHealth";

    public static final String AMAZON_VC_PROMOTION = "https://vendorcentral.amazon.com/hz/vendor/members/promotions/list/home?ref_=vc_xx_subNav";

}
