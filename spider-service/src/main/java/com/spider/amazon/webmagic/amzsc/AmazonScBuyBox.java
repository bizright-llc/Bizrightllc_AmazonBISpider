package com.spider.amazon.webmagic.amzsc;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.common.exception.ServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spider.amazon.batch.scbuyboxinfo.CsvBatchConfigForAmzScBuyBox;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.entity.Cookie;
import com.spider.amazon.model.Consts;
import com.spider.amazon.remote.api.SpiderUrl;
import com.spider.amazon.service.CommonSettingService;
import com.spider.amazon.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

/**
 * Amazon卖家中心每周BuyBox数据抓取
 *
 * Download the data of the dat two days ago
 */
@Component
@Slf4j
public class AmazonScBuyBox implements PageProcessor {

    //
//    private static final String jsonPathSc = "C:\\Program Files\\Java\\BiSpider\\cookieSc.json";
    private static final String jsonPathSc = "/Users/shaochinlin/Documents/Bizright/BI/BiSpider/cookieSc.json";

    private SpiderConfig spiderConfig;

    private CommonSettingService commonSettingService;

    // parse date default be two days ago
    private LocalDate parseDate = LocalDate.now().minusDays(2);

    @Autowired
    public AmazonScBuyBox(SpiderConfig spiderConfig, CommonSettingService commonSettingService) {
        this.spiderConfig = spiderConfig;
        this.commonSettingService = commonSettingService;
    }

//    @Autowired
//    private CookiesUtils cookiesUtils;

    private Site site = Site
            .me()
            .setRetryTimes(3)
            .setDomain(SpiderUrl.SPIDER_INDEX)
            .setSleepTime(3000)
            .setUserAgent(
                    "User-Agent:Mozilla/5.0(Macintosh;IntelMacOSX10_7_0)AppleWebKit/535.11(KHTML,likeGecko)Chrome/17.0.963.56Safari/535.11");

    /**
     * 设置网站信息
     *
     * @return
     */
    public Site getSite() {
        return site;
    }

    public void setParseDate(LocalDate date){
        if(date != null){

            if(date.compareTo(LocalDate.now().minusDays(2)) > 0){
                log.info("[AmazonScBuyBox] [setParseDate] cannot set parse date after two days before");
                parseDate = LocalDate.now().minusDays(2);
            }else{
                parseDate = date;
            }

        }else{
            parseDate = LocalDate.now().minusDays(2);
        }
    }

    /**
     * 页面抓取过程
     *
     * @param page page
     */
    public void process(Page page) {
        if (log.isInfoEnabled()) {
            log.info("0.step21=>进入抓取");
        }

        ObjectMapper objectMapper = new ObjectMapper();

        String downloadDir = spiderConfig.getScBuyBoxDownloadPath();

        // 1.建立WebDriver
        WebDriver driver = WebDriverUtils.getWebDriver(spiderConfig.getChromeDriverPath(), downloadDir, false);

        try {

            driver.manage().deleteAllCookies();

            // 1.1设置页面超时等待时间,20S
            driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

            // 2.初始打开页面
            driver.navigate().to(SpiderUrl.AMAZON_SC_404);

            // 3.add Cookies 在工具类中解析json
            List<Cookie> cookies = commonSettingService.getAmazonSCCookies();

            List<org.openqa.selenium.Cookie> sCookies = CookiesUtils.cookiesToSeleniumCookies(cookies);

            driver.manage().deleteAllCookies();
            WebDriverUtils.addSeleniumCookies(driver, sCookies);

            if(!WebDriverUtils.checkAmazonSCCookiesValid(driver)){
                driver.manage().deleteAllCookies();
                WebDriverUtils.getAmazonSCCookies(driver);

                List<Cookie> driverCookies = CookiesUtils.seleniumCookieToCookie(driver.manage().getCookies());

                String newCookiesStr = objectMapper.writeValueAsString(driverCookies);

                commonSettingService.setValue(Consts.AMAZON_SC_COOKIES, newCookiesStr, "system");
            }

            // 4.重定向跳转
            // download the day before yesterday data
//             构造查询日期数据，获取上一个自然周的数据
            String dayBeforeYesterdayStr = parseDate.format(DateTimeFormatter.ofPattern(DateFormat.YEAR_MONTH_DAY_MMddyyyy));

            String filterFromDate = dayBeforeYesterdayStr;
            String filterToDate = dayBeforeYesterdayStr;
            final String redirectUrl = SpiderUrl.SPIDER_SC_BUYBOX.replace("{filterFromDate}", filterFromDate)
                    .replace("{filterToDate}", filterToDate)
                    .replace("{fromDate}", dayBeforeYesterdayStr)
                    .replace("{toDate}", dayBeforeYesterdayStr);
            driver.navigate().to(redirectUrl);

            sleep(10000);

//            // 获得cookie
//            Set<org.openqa.selenium.Cookie> coo = driver.manage().getCookies();
//            System.out.println(coo);


            // 5.进行操作点击下载Excel,抓取标题
//            WebElement titleElement = driver.findElement(By.xpath("//title"));
//            String title = titleElement.getAttribute("text");

            // 6.抓取点击下载元素进行点击
            // 判断是否出现了Download按钮,未在规定时间内出现重新刷新页面
            WebElement downloadButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='export']"), 10);
            downloadButtonElement.click();

            // 7.抓取CSV元素生成并进行点击
            WebElement detailCsvDownloadButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='downloadCSV']"), 10);
            detailCsvDownloadButtonElement.click();

            try {
                sleep(10000);
            } catch (InterruptedException e) {
                throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
            }

            // 8. Change the last download filename
            File downloadFile = FileUtils.getLatestFileWithNameFromDir(downloadDir, CsvBatchConfigForAmzScBuyBox.FILE_NAME);
            Path oldFilePath = Paths.get(downloadFile.getPath());

            String fileDateStr = DateUtil.format(DateUtil.parse(dayBeforeYesterdayStr, DateFormat.YEAR_MONTH_DAY_MMddyyyy), DateFormat.YEAR_MONTH_DAY);

            String newFileName = StrUtil.concat(true, CsvBatchConfigForAmzScBuyBox.FILE_NAME, "-", fileDateStr);

            Files.move(oldFilePath, oldFilePath.resolveSibling(newFileName + ".csv"));


        } catch (Exception e) {
            log.info("[AmazonScBuyBox download data process failed]", e);
            driver.quit();
            e.printStackTrace();
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
        } finally {
            driver.quit();
        }

        if (log.isInfoEnabled()) {
            log.info("1.step84=>抓取结束");
        }

    }

//    public static void main(String[] args) {
//
//        System.out.println("0.step67=>抓取程序开启。");
//
//        Spider.create(new AmazonScBuyBox())
//                .addUrl(SpiderUrl.SPIDER_SC_INDEX)
//                .run();
//
//        System.out.println("end.step93=>抓取程序结束。");
//
//    }

}

