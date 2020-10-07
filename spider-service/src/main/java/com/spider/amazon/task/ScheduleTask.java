package com.spider.amazon.task;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.batch.sc.buyboxinfo.CsvBatchConfigForAmzScBuyBox;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.cons.PageQryType;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.cons.SqlResult;
import com.spider.amazon.dto.*;
import com.spider.amazon.remote.api.SpiderUrl;
import com.spider.amazon.service.*;
import com.spider.amazon.service.impl.SpringBatchCallServiceImpl;
import com.spider.amazon.utils.FileUtils;
import com.spider.amazon.webmagic.amz.AmazonAdConsumeProcessor;
import com.spider.amazon.webmagic.amzsc.AmazonScBuyBox;
import com.spider.amazon.webmagic.amzsc.AmazonScFbaInventory;
import com.spider.amazon.webmagic.amzvc.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.ExceptionHandler;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

/**
 * 定时任务列表
 */
// Comment when testing
//@Component
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
    private FbaInventoryReportDealService fbaInventoryReportDealService;

    @Autowired
    private IInvoiceService invoiceService;

    @Autowired
    private CommonSettingService commonSettingService;


    private final static int pageSize = 100000;
    private final static int minPageSize = 10;
    private final static int pageNo = 1;

    private final static String AMAZON_VC_INVENTORY_HEALTH_FILE_NAME = "Inventory Health_US";
    private final static String AMAZON_VC_DAILY_SALES_FILE_NAME = "Sales Diagnostic_Detail View_US";
    private final static String AMAZON_SC_BUY_BOX_FILE_NAME = "BusinessReport";

    /**
     * Fba库存日报处理属性
     */
    private final static String fbaInventoryFileName = "Fba_Inventory";
    private static final int offerSetDay = 0;
    private static final int invoiceOfferSetDay=0;
    private static final int poHeaderOfferSetDay=0;
    private static final int vendorPoOfferSetDay=0;


    /**
     * 测试定时任务
     */
    @Scheduled(cron = "0 0 0 */1 * *")
    public void task01(){
        System.out.println("task01 run...");
    }

    /**
     * 测试配置文件加载
     */
//    @Scheduled(cron = "0/30 * * * * ?")
//    public void task02(){
//        log.info("test task02 run...");
//    }

    /**
     * 定时下载Amazon SC FBA INV数据
     */
    @Scheduled(cron = "0 40 1 * * ?")
    public void schedulerScFbaInventory() {
        log.info("0.step56=>开始执行［schedulerScFbaInventory］");
        Spider spider = Spider.create(new AmazonScFbaInventory(spiderConfig, commonSettingService));
        spider.addUrl(spiderConfig.getSpiderIndex());
        spider.setExitWhenComplete(true);
        spider.run();
    }


    /**
     * 定时下载Amazon SC Buy Box数据
     */
    @Scheduled(cron = "0 50 1 * * 1")
    @Retryable(value = Exception.class, maxAttempts = 3)
    public void schedulerScBuyBox() {
        log.info("0.step56=>开始执行［schedulerScBuyBox］");
        Spider spider = Spider.create(new AmazonScBuyBox(spiderConfig, commonSettingService));
        spider.addUrl(spiderConfig.getSpiderIndex());
        spider.setExitWhenComplete(true);
        spider.run();
    }

    /**
     * 定时下载Amazon VC daily sales manufacturing view
     */
    @Scheduled(cron = "0 10 2 * * ?")
    @Retryable(value = Exception.class, maxAttempts = 3)
    public void schedulerVcDailySales() throws InterruptedException {
        log.info("0.step56=>开始执行［schedulerVcDailySales］");

        try{
            Spider spider = Spider.create(new AmazonVcDailySales(spiderConfig, commonSettingService));
            spider.addUrl(spiderConfig.getSpiderIndex());
            spider.setExitWhenComplete(true);
            spider.run();
        }catch (Exception ex){
            log.error("[schedulerVcDailySales] failed", ex);
            throw ex;
        }
    }

    /**
     * Download Amazon VC daily inventory files
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Retryable(value = Exception.class, maxAttempts = 3)
    public void schedulerVcDailyInventorySourcing() {
        log.info("0.step64=>开始执行［schedulerVcDailyInventorySourcing］");
        Spider spider = Spider.create(new AmazonVcDailyInventoryHealth(spiderConfig, commonSettingService));
        spider.addUrl(spiderConfig.getSpiderIndex());
        spider.setExitWhenComplete(true);
        spider.run();
    }

    /**
     * Download Amazon VC daily inventory manufacturing view
     */
//    @Scheduled(cron = "0 10 3 * * ?")
//    public void schedulerVcDailyInventoryManufacturing() {
//        log.info("0.step64=>开始执行［schedulerVcDailyInventoryManufacturing］");
//        Spider spider = Spider.create(new AmazonVcDailyInventoryHealthManufacturing(spiderConfig, commonSettingService));
//        spider.addUrl(spiderConfig.getSpiderIndex());
//        spider.setExitWhenComplete(true);
//        spider.run();
//    }

    /**
     * 定时下载Amazon VC Promotion info and Promotion product info
     * Run at every monday
     */
    @Scheduled(cron = "0 0 4 * * ?")
    @Retryable(Exception.class)
    public void schedulerVcDailyPromotionInfo() {
        log.info("0.step234=>开始执行［schedulerVcDailyPromotionInfo］");
        Spider spider = Spider.create(new AmazonVcPromotionsProcessor(spiderConfig, commonSettingService));
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
     * TODO: Need to refactor
     */
//    @Scheduled(cron = "0 50 3 * * ?")
//    public void schedulerGetPOHeader() {
//        log.info("0.step130=>开始执行［schedulerGetPOHeader］");
//        poHeaderService.getPOHeader(GetPOHeaderDTO.builder()
//                .pageNo(pageNo).pageSize(pageSize).asin("").poDate(DateUtil.format(DateUtil.offset(DateUtil.date(), DateField.DAY_OF_MONTH, poHeaderOfferSetDay), "yyyyMMdd")).build());
//    }

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


    /**
     * 定时处理Amazon VC 销量报表
     */
//    @Scheduled(fixedDelay = 60000)
    public void schedulerVcSalesDataDeal() {
        log.info("0.step112=>开始执行［schedulerVcSalesDataDeal］");

        if(checkVCDailySalesFileExist()){
            log.info("Amazon VC Daily Sales file exist");
            springBatchCallServiceImpl.callVcSalesReportDataDeal();
        }else{
            log.info("No Amazon VC Daily Sales file");
        }
    }

    /**
     * schedulerVcDailyInventoryDataDeal
     * 定时处理Amazon VC 每日库存报表
     */
    @Scheduled(fixedDelay = 60000)
    public void schedulerVcDailyInventoryDataDeal() {
        log.info("0.step112=>开始执行［schedulerVcDailyInventoryDataDeal］");

        if(checkVCInventoryFileExist()){
            log.info("Amazon VC Inventory Sales file exist");
            springBatchCallServiceImpl.callVcInventoryReportDataDeal();
        }else{
            log.info("No Amazon VC Inventory Health file");
        }

    }

    /**
     * 定时处理Amazon SC BuyBox报表
     */
    @Scheduled(fixedDelay = 60000)
    public void schedulerScBuyBoxDeal() {
        log.info("[schedulerScBuyBoxDeal] start schedule ");

        if(checkSCBuyBoxFileExist()){
            log.info("[schedulerScBuyBoxDeal] Find the file, start process");
            springBatchCallServiceImpl.callScBuyBoxReportDataDeal();
        }else{
            log.info("[schedulerScBuyBoxDeal] Did not find any file to process");
        }

        log.info("[schedulerScBuyBoxDeal] Complete");

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
     * Deal Fba Inventory file
     */
    @Scheduled(fixedDelay = 60000)
    public void schedulerScFbaInventoryDeal() {
        log.info("[schedulerScFbaInventoryDeal] start process");

        String filenameExist = checkSCFBAInventoryFileExist();

        if(StringUtils.isNotEmpty(filenameExist)){

            fbaInventoryReportDealService.dealFbaInventoryReport(filenameExist, spiderConfig.getScFBAInventoryDownloadPath());

            log.info("[schedulerScFbaInventoryDeal] complete process");

        }else{
            log.info("[schedulerScFbaInventoryDeal] no file found to process");
        }
    }

    /**
     * 定时消耗广告
     */
//    @Scheduled(cron = "0 0/1 * * * ? ")
    public void schedulerAdConsumer() {

        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();

        // 调用api获取代理IP列表
        List<Proxy> proxies = null;
        try {
            proxies = AmazonAdConsumeProcessor.buildProxyIP();
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpClientDownloader.setProxyProvider(new SimpleProxyProvider(proxies));

        Spider.create(new AmazonAdConsumeProcessor(spiderConfig))
                .addUrl(SpiderUrl.AMAZON_INDEX)
                .setDownloader(httpClientDownloader)
                .run();
    }

//    @ExceptionHandler
//    public void handle(Exception e){
//        log.error("[schedule failed]", e);
//    }

    /**
     * Check if any file haven't been process
     *
     * @return
     */
    private boolean checkVCInventoryFileExist(){

        class MyFileFilter implements FileFilter {

            public boolean accept(File f) {
                if (f.getName().contains(AMAZON_VC_INVENTORY_HEALTH_FILE_NAME) && !f.getName().contains("Daily")) {
                    if(FileUtils.getFileExtension(f.getName()).equalsIgnoreCase("csv")){
                        return true;
                    }
                }
                return false;
            }
        }

        MyFileFilter filter = new MyFileFilter();

        File[] files = FileUtils.getFileFromDir(spiderConfig.getVcHealthInventoryDownloadPath(), filter);

        File file = files != null && files.length > 0 ? files[0] : null;

        // File exist
        if (file != null && FileUtil.exist(file.getPath())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check if any file haven't been process
     *
     * @return
     */
    private boolean checkVCDailySalesFileExist(){

        class MyFileFilter implements FileFilter {

            public boolean accept(File f) {
                if (f.getName().contains(AMAZON_VC_DAILY_SALES_FILE_NAME) && !f.getName().toLowerCase().contains("Daily".toLowerCase())) {
                    if(FileUtils.getFileExtension(f.getName()).equalsIgnoreCase("csv")){
                        return true;
                    }
                }
                return false;
            }
        }

        MyFileFilter filter = new MyFileFilter();

        File[] files = FileUtils.getFileFromDir(spiderConfig.getVcDailySalesDownloadPath(), filter);

        File file = files != null && files.length > 0 ? files[0] : null;

        // File exist
        if (file != null && FileUtil.exist(file.getPath())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkSCBuyBoxFileExist(){

        class MyFileFilter implements FileFilter {

            public boolean accept(File f) {

                final String regex = "BusinessReport-\\d{8}.csv";

                final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

                String fileName = f.getName();

                Matcher matcher = pattern.matcher(fileName);

                if (f.getName().contains(AMAZON_SC_BUY_BOX_FILE_NAME) && !f.getName().contains(CsvBatchConfigForAmzScBuyBox.COMPLETE_MARK) && matcher.find()) {
                    if(FileUtils.getFileExtension(f.getName()).equalsIgnoreCase("csv")){
                        return true;
                    }
                }
                return false;
            }
        }

        MyFileFilter filter = new MyFileFilter();

        File[] files = FileUtils.getFileFromDir(spiderConfig.getVcDailySalesDownloadPath(), filter);

        File file = files != null && files.length > 0 ? files[0] : null;

        // File exist
        if (file != null && FileUtil.exist(file.getPath())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check if there are any file not process
     * return first find file name if exist
     * ruturn empty string if no file exist
     * @return
     */
    private String checkSCFBAInventoryFileExist(){

        class MyFileFilter implements FileFilter {

            public boolean accept(File f) {

                final String regex = String.format("%s-\\d{8}.csv", AmazonScFbaInventory.FILE_RENAME);

                final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

                String fileName = f.getName();

                Matcher matcher = pattern.matcher(fileName);

                if (f.getName().contains(AMAZON_SC_BUY_BOX_FILE_NAME) && !f.getName().contains(CsvBatchConfigForAmzScBuyBox.COMPLETE_MARK) && matcher.find()) {
                    if(FileUtils.getFileExtension(f.getName()).equalsIgnoreCase("csv")){
                        return true;
                    }
                }
                return false;
            }
        }

        MyFileFilter filter = new MyFileFilter();

        File[] files = FileUtils.getFileFromDir(spiderConfig.getVcDailySalesDownloadPath(), filter);

        File file = files != null && files.length > 0 ? files[0] : null;

        // File exist
        if (file != null && FileUtil.exist(file.getPath())) {
            return file.getName();
        } else {
            return "";
        }
    }

}
