package com.spider.amazon.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 爬虫配置文件属性注入类
 *
 */
@Configuration
@PropertySource(value = {"classpath:spider.properties"})
@Data
public class SpiderConfig {
    @Value("${spider.amazon.vc.index}/")
    private String spiderIndex;

    @Value("${spider.amazon.vc.addr.sales}")
    private String spiderAmzVcSales;

    @Value("${spider.amazon.vc.addr.inventory}")
    private String spiderAmzVcInv;

    @Value("${spider.amazon.vc.addr.po}")
    private String spiderAmzVcPo;

    @Value("${spider.amazon.haw.addr.index}")
    private String spiderHawIndex;

    @Value("${spider.download.path}")
    private String downloadPath;

    @Value("${spider.download.dailysales.path}")
    private String vcDailySalesDownloadPath;

    @Value("${spider.download.healthinventory.path}")
    private String vcHealthInventoryDownloadPath;

    @Value("${spider.download.buybox.path}")
    private String scBuyBoxDownloadPath;

    @Value("${spider.download.fbainventory.path}")
    private String scFBAInventoryDownloadPath;

    @Value("${spider.amazon.vc.cookie}")
    private String amzVcCookieFilepath;

    @Value("${spider.amazon.sc.cookie}")
    private String amzScCookieFilepath;

    @Value("${spider.driver.chrome.path}")
    private String chromeDriverPath;

    @Value("${spider.driver.proxy.filepath.chrome}")
    private String chromeProxyFilepath;

    @Value("${spider.driver.proxy.chrome}")
    private Boolean chromeProxy;
}
