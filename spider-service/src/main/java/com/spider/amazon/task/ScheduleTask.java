package com.spider.amazon.task;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.cons.PageQryType;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.cons.SqlResult;
import com.spider.amazon.dto.*;
import com.spider.amazon.remote.api.SpiderUrl;
import com.spider.amazon.service.*;
import com.spider.amazon.service.impl.SpringBatchCallServiceImpl;
import com.spider.amazon.webmagic.*;
import com.spider.amazon.webmagic.amz.AmazonAdConsumeProcessor;
import com.spider.amazon.webmagic.amzvc.AmazonVcPromotionsPipeline;
import com.spider.amazon.webmagic.amzvc.AmazonVcPromotionsProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;

import java.io.IOException;
import java.util.List;

/**
 * 定时任务列表
 */
@Component
@Slf4j
public class ScheduleTask {


    @Autowired
    private SpiderConfig spiderConfig;

    @Autowired
    private SpringBatchCallServiceImpl springBatchCallServiceImpl;

    @Autowired
    private IPoHeaderService poHeaderService;

    @Autowired
    private IWarehouseTransferService warehouseTransferService;

    @Autowired
    private IVendorPoInfoService vendorPoInfoService;

    @Autowired
    private IInventoryDataDailySnapShotService inventoryDataDailySnapShotService;

    @Autowired
    private ISkuInfoService skuInfoService;

    @Autowired
    private IFbaInventoryReportDealService fbaInventoryReportDealService;

    @Autowired
    private IInvoiceService invoiceService;


    private final static int pageSize = 100000;
    private final static int minPageSize = 10;
    private final static int pageNo = 1;

    /**
     * Fba库存日报处理属性
     */
    private final static String fbaInventoryFileName = "Fba_Inventory";
    private static final String filePath = "C:\\Users\\paulin.f\\Downloads\\";
    private static final int offerSetDay = 0;
    private static final int invoiceOfferSetDay=0;
    private static final int poHeaderOfferSetDay=0;
    private static final int vendorPoOfferSetDay=0;


//    /**
//     * 测试定时任务
//     */
//    @Scheduled(cron = "*/60 * * * * ?")
//    public void task01(){
//        System.out.println("task01 run...");
//    }

    /**
     * 测试配置文件加载
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void task02(){
        log.info("test task02 run...");
    }

    /**
     * 定时下载Amazon SC FBA INV数据
     */
    @Scheduled(cron = "0 40 1 * * ?")
    public void schedulerScFbaInventory() {
        log.info("0.step56=>开始执行［schedulerScFbaInventory］");
        Spider spider = Spider.create(new AmazonScFbaInventory());
        spider.addUrl(spiderConfig.getSpiderIndex());
        spider.setExitWhenComplete(true);
        spider.run();
    }


    /**
     * 定时下载Amazon SC Buy Box数据
     */
    @Scheduled(cron = "0 50 1 * * 1")
    public void schedulerScBuyBox() {
        log.info("0.step56=>开始执行［schedulerScBuyBox］");
        Spider spider = Spider.create(new AmazonScBuyBox());
        spider.addUrl(spiderConfig.getSpiderIndex());
        spider.setExitWhenComplete(true);
        spider.run();
    }

    /**
     * 定时下载Amazon VC 每日销量报表
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void schedulerVcDailySales() {
        log.info("0.step56=>开始执行［schedulerVcDailySales］");
        Spider spider = Spider.create(new AmazonVcDailySales());
        spider.addUrl(spiderConfig.getSpiderIndex());
        spider.setExitWhenComplete(true);
        spider.run();
    }


    /**
     * 定时下载Amazon VC 每周销量报表
     */
    @Scheduled(cron = "0 0 4 * * 1")
    public void schedulerVcWeeklySales() {
        log.info("0.step56=>开始执行［schedulerVcWeeklySales］");
        Spider spider = Spider.create(new AmazonVcWeeklySales());
        spider.addUrl(spiderConfig.getSpiderIndex());
        spider.setExitWhenComplete(true);
        spider.run();
    }

    /**
     * 定时下载Amazon VC 每日库存报表
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void schedulerVcDailyInventory() {
        log.info("0.step64=>开始执行［schedulerVcDailyInventory］");
        Spider spider = Spider.create(new AmazonVcPoInfo());
        spider.addUrl(spiderConfig.getSpiderIndex());
        spider.setExitWhenComplete(true);
        spider.run();
    }

    /**
     * 定时下载Amazon VC Promotion info and Promotion product info
     * Run at every monday
     */
    @Scheduled(cron = "0 0 4 * * MON")
    public void schedulerVcDailyPromotionInfo() {
        log.info("0.step234=>开始执行［schedulerVcDailyPromotionInfo］");
        Spider spider = Spider.create(new AmazonVcPromotionsProcessor());
        spider.addPipeline(new AmazonVcPromotionsPipeline());
        spider.addUrl(spiderConfig.getSpiderIndex());
        spider.setExitWhenComplete(true);
        spider.run();
    }

    /**
     * 定时下载BOPAPI VC Po信息
     */
    @Scheduled(cron = "0 30 3 * * ?")
    public void schedulerVcPoInfo() {
        log.info("0.step88=>开始执行［schedulerVcPoInfo］");
        vendorPoInfoService.getVcPoInfo(VcPoInfoDTO.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .vendor("")
                .asin("")
                .lastUpdate("")
                .poNum("")
                .enterDate(DateUtil.format(DateUtil.offset(DateUtil.date(),  DateField.DAY_OF_MONTH, poHeaderOfferSetDay), "yyyyMMdd"))
                .build());
    }

    /**
     * 定时下载BOPAPI FBA_PO信息
     * TODO 该BOP API 没有根据时间来筛选的条件，只能全量替换，慎用
     */
    @Scheduled(cron = "0 40 3 * * ?")
    public void schedulerGetWarehouseTransfer() {
        log.info("0.step114=>开始执行［schedulerGetWarehouseTransfer］");
        warehouseTransferService.getWarehouseTransfer(GetWarehouseTransferDTO.builder()
                .pageNo(pageNo).pageSize(pageSize).build());
    }

    /**
     * 定时下载BOPAPI LA_PO信息
     */
    @Scheduled(cron = "0 50 3 * * ?")
    public void schedulerGetPOHeader() {
        log.info("0.step130=>开始执行［schedulerGetPOHeader］");
        poHeaderService.getPOHeader(GetPOHeaderDTO.builder()
                .pageNo(pageNo).pageSize(pageSize).asin("").poDate(DateUtil.format(DateUtil.offset(DateUtil.date(), DateField.DAY_OF_MONTH, poHeaderOfferSetDay), "yyyyMMdd")).build());
    }

    /**
     * 定时下载BOPAPI LA_INV信息
     * TODO BOP该API很慢，当个数大的时候API直接返回异常
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void schedulerGetInventoryData() {
        log.info("0.step154=>开始执行［schedulerGetInventoryData］");
        inventoryDataDailySnapShotService.getInventoryDataDailySnapShot(GetInventoryDataDailySnapShotDTO.builder()
                .pageNo(pageNo).pageSize(minPageSize).getType(PageQryType.QRY_ALL).build());
    }

    /**
     * 定时下载BOPAPI Invoice信息
     */
    @Scheduled(cron = "0 10 4 * * ?")
    public void schedulerGetInvoice() {
        log.info("0.step203=>开始执行［schedulerGetInvoice］");
        invoiceService.getInvoice(GetInvoiceDTO.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .asin("")
                .invoiceDate(DateUtil.format(DateUtil.offset(DateUtil.date(), DateField.DAY_OF_MONTH, invoiceOfferSetDay), "yyyyMMdd"))
                .channel("")
                .build());
    }


    /**schedulerVcDailyInventoryDataDeal
     * 定时处理Amazon VC 每日库存报表
     */
    @Scheduled(cron = "0 0 7-12 * * ?  ")
    public void schedulerVcDailyInventoryDataDeal() {
        log.info("0.step112=>开始执行［schedulerVcDailyInventoryDataDeal］");
        springBatchCallServiceImpl.callVcInventoryReportDataDeal();
    }

    /**
     * 定时处理Amazon VC 销量报表
     */
    @Scheduled(cron = "0 5 7-12 * * ? ")
    public void schedulerVcSalesDataDeal() {
        log.info("0.step112=>开始执行［schedulerVcSalesDataDeal］");
        springBatchCallServiceImpl.callVcSalesReportDataDeal();
    }

    /**
     * 定时处理Amazon SC BuyBox报表
     */
    @Scheduled(cron = "0 10 7-12 * * ? ")
    public void schedulerScBuyBoxDeal() {
        log.info("0.step112=>开始执行［schedulerScBuyBoxDeal］");
        springBatchCallServiceImpl.callScBuyBoxReportDataDeal();
    }

    /**
     * 定时处理Sku通用信息导库处理
     */
    @Scheduled(cron = "0 15 7-23 * * ? ")
    public void schedulerIntoSkuCommomInfoByCopy() {
        log.info("0.step112=>开始执行［schedulerIntoSkuCommomInfoByCopy］");
        int result = skuInfoService.intoSkuCommonInfoByCopy();
        if (result < SqlResult.NO_RECORD) {
            throw new ServiceException(RespErrorEnum.SERVICE_DATA_EXPC.getSubStatusCode(), RespErrorEnum.SERVICE_DATA_EXPC.getSubStatusMsg());
        }
    }

    /**
     * 定时处理FBA INV信息入库
     */
    @Scheduled(cron = "0 20 7-12 * * ? ")
    public void schedulerScFbaInventoryDeal() {
        log.info("0.step233=>开始执行［schedulerScFbaInventoryDeal］");
        fbaInventoryReportDealService.dealFbaInventoryReport(StrUtil.concat(true,fbaInventoryFileName,"-",DateUtil.format(DateUtil.offsetDay(DateUtil.date(),offerSetDay), DateFormat.YEAR_MONTH_DAY),".csv"),filePath,offerSetDay);
    }


    /**
     * 定时消耗广告
     */
//    @Scheduled(cron = "0 0/1 * * * ? ")
    public void schedulerAdConsumer() {

        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();
//            调用api获取代理IP列表
        List<Proxy> proxies = null;
        try {
            proxies = AmazonAdConsumeProcessor.buildProxyIP();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientDownloader.setProxyProvider(new SimpleProxyProvider(proxies));

        Spider.create(new AmazonAdConsumeProcessor())
                .addUrl(SpiderUrl.AMAZON_INDEX)
                .setDownloader(httpClientDownloader)
                .run();
    }

}
