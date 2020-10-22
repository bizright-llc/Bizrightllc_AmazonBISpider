package com.spider.amazon.webmagic.amzvc;

import com.common.exception.ServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.entity.Cookie;
import com.spider.amazon.model.Consts;
import com.spider.amazon.remote.api.SpiderUrl;
import com.spider.amazon.service.CommonSettingService;
import com.spider.amazon.utils.CookiesUtils;
import com.spider.amazon.utils.WebDriverUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

/**
 * Amazon供应商中心每日销量数据抓取
 * Sales Diagnostic
 * Download csv From Amazon Vendor central
 * https://vendorcentral.amazon.com/analytics/dashboard/salesDiagnostic
 *
 * Distribute View : Manufacturing, Sourcing
 * Sales View: Shipped COGS
 * Reporting Range: Daily
 */
@Component
@Slf4j
public class AmazonVcDailySales implements PageProcessor {

    private final static String manufacturingViewXPath = "//*[@id=\"dashboard-filter-distributorView\"]/div/awsui-button-dropdown/div/div/ul/li[contains(@data-testid,'manufacturer')]";
    private final static String sourcingViewXPath = "//*[@id=\"dashboard-filter-distributorView\"]/div/awsui-button-dropdown/div/div/ul/li[contains(@data-testid,'sourcing')]";

    private final static String salesViewShippedCOGSLevelXPath = "//*[@id='dashboard-filter-viewFilter']//awsui-button-dropdown//ul/li[contains(@data-testid, \"shippedCOGSLevel\")]";

    // date picker xpath
    private final static String dateRangeFromDateXPath = "(//input[contains(@class, 'date-picker-input')])[1]";
    private final static String dateRangeToDateXPath = "(//input[contains(@class, 'date-picker-input')])[2]";
    private final static String datePickerPreviousBtnXPath = "//a[contains(@class, 'react-datepicker__navigation--previous')]";
    private final static String datePickerNextBtnXPath = "//a[contains(@class, 'react-datepicker__navigation--next')]";
    private final static String datePickerLeftMonthXPath = "(//div[contains(@class, 'react-datepicker__current-month')])[1]";
    private final static String datePickerRightMonthXPath = "(//div[contains(@class, 'react-datepicker__current-month')])[2]";

    private final String detailCsvXPath = "//*[@id=\"downloadButton\"]/awsui-button-dropdown/div/div/ul/li/ul[contains(@aria-label, 'Detail View')]/li[contains(@data-testid, 'salesDiagnosticDetail_csv')]";

    /**
     * If parse date is set, parse the date
     * if not, just download the newest date data
     */
    private LocalDate parseDate;

    private SpiderConfig spiderConfig;

    private CommonSettingService commonSettingService;

    @Autowired
    public AmazonVcDailySales(SpiderConfig spiderConfig, CommonSettingService commonSettingService) {
        this.spiderConfig = spiderConfig;
        this.commonSettingService = commonSettingService;
    }

    public void setParseDate(LocalDate parseDate){
        this.parseDate = parseDate;
    }

    private Site site = Site
            .me()
            .setRetryTimes(3)
            .setDomain("https://vendorcentral.amazon.com/analytics/dashboard/salesDiagnostic")
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

    /**
     * 页面抓取过程
     *
     * @param page page
     */
    public void process(Page page) {
        if (log.isInfoEnabled()) {
            log.info("0.step21=>进入抓取");
        }

        log.info("[process] parse date {}", parseDate == null ? "default" : parseDate.toString());

        // 1.建立WebDriver
        System.setProperty("webdriver.chrome.driver", spiderConfig.getChromeDriverPath());

        String downloadFilePath = spiderConfig.getVcDailySalesDownloadPath();

        WebDriver driver = WebDriverUtils.getWebDriver(downloadFilePath, true);

        ObjectMapper objectMapper = new ObjectMapper();

        try {

            // 1.1设置页面超时等待时间,20S
            driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

            WebDriverWait wait = new WebDriverWait(driver, 20);

            // 2.初始打开页面
            driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
            driver.get(SpiderUrl.AMAZON_VC_404);

            // Set cookie
            driver.manage().deleteAllCookies();

            List<Cookie> cookies = commonSettingService.getAmazonVCCookies();

            List<org.openqa.selenium.Cookie> savedCookies = CookiesUtils.cookiesToSeleniumCookies(cookies);

            WebDriverUtils.addSeleniumCookies(driver, savedCookies);

            // cookies are not valid
            if(!WebDriverUtils.checkAmazonVCCookiesValid(driver)){
                driver.manage().deleteAllCookies();
                WebDriverUtils.getAmazonVCCookies(driver);

                List<Cookie> driverCookies = CookiesUtils.seleniumCookieToCookie(driver.manage().getCookies());

                String newCookiesStr = objectMapper.writeValueAsString(driverCookies);

                commonSettingService.setValue(Consts.AMAZON_VC_COOKIES, newCookiesStr, "system");
            }

            List<org.openqa.selenium.Cookie> latestVCCookies = driver.manage().getCookies().stream().collect(Collectors.toList());

            CompletableFuture<Void> manufacturingViewCompletableFuture = CompletableFuture.runAsync(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    WebDriver manuDriver = WebDriverUtils.getWebDriver(downloadFilePath, false);

                    try{

                        manuDriver.navigate().to(SpiderUrl.AMAZON_VC_404);

                        WebDriverUtils.addSeleniumCookies(manuDriver, latestVCCookies);

                        //4.1 navigate to sales daily page
                        navigateToPage(manuDriver);

                        //4.1点击日期选择按钮, Reporting range
                        WebElement reportingRangeButtonElement = WebDriverUtils.expWaitForElement(manuDriver, By.xpath("//*[@id='dashboard-filter-reportingRange']//awsui-button-dropdown//awsui-button/button"), 20);
                        if (log.isInfoEnabled()) {
                            log.info("1.step105=>reportingRangeButtonElement:" + reportingRangeButtonElement.toString());
                        }
                        WebDriverUtils.elementClick(reportingRangeButtonElement);

                        //4.2点击选择daily
                        WebElement dailySelectElement = WebDriverUtils.expWaitForElement(manuDriver, By.xpath("//*[@id=\"dashboard-filter-reportingRange\"]/div/awsui-button-dropdown//ul/li[contains(@data-testid, 'DAILY')]"), 10);
                        if (log.isInfoEnabled()) {
                            log.info("2.step112=>dailySelectElement:" + dailySelectElement.toString());
                        }
                        dailySelectElement.click();

                        if(parseDate != null){
                            WebElement toDateEle = WebDriverUtils.expWaitForElement(manuDriver, By.xpath(dateRangeToDateXPath), 10);
                            toDateEle.click();

                            WebDriverUtils.randomSleep();

                            // select month
                            WebElement leftMonthEle = WebDriverUtils.expWaitForElement(manuDriver, By.xpath(datePickerLeftMonthXPath), 10);
                            String eleText = leftMonthEle.getText().toLowerCase();
                            String parseDateMonthStr = parseDate.getMonth().toString().toLowerCase();

                            while (!eleText.contains(parseDateMonthStr)){

                                WebElement previousEle = WebDriverUtils.expWaitForElement(manuDriver, By.xpath(datePickerPreviousBtnXPath), 10);

                                WebDriverUtils.elementClick(previousEle);

                                WebDriverUtils.randomSleep();

                                leftMonthEle = WebDriverUtils.expWaitForElement(manuDriver, By.xpath(datePickerLeftMonthXPath), 10);
                                eleText = leftMonthEle.getText().toLowerCase();
                            }

                            // select date
                            String leftDatePickerDateXPathFormat = "/html/body/div[4]/div/div[2]//div[@aria-label='day-%s']";
                            int day = parseDate.getDayOfMonth();
                            String dateXPath = String.format(leftDatePickerDateXPathFormat, day);
                            List<WebElement> elements = WebDriverUtils.expWaitForElements(manuDriver, By.xpath(dateXPath), 10);

                            WebElement dateEle = null;

                            if (elements.size() > 1){
                                if(day <= 7 ){
                                    dateEle = elements.get(0);
                                }else{
                                    dateEle = elements.get(1);
                                }
                            }else{
                                dateEle = elements.get(0);
                            }

                            // Bug
                            WebDriverUtils.elementClick(dateEle);
                            WebDriverUtils.randomSleep();
                        }

                        // 4.21 Choose DistributeView View
                        WebElement distributeViewViewButtonElement = WebDriverUtils.expWaitForElement(manuDriver, By.xpath("//*[@id='dashboard-filter-distributorView']//awsui-button-dropdown//button"), 10);
                        if (log.isInfoEnabled()) {
                            log.info("1.step105=>distributeViewViewButtonElement:" + distributeViewViewButtonElement.toString());
                        }
                        distributeViewViewButtonElement.click();

                        // 4.22点击选择View
                        if (log.isInfoEnabled()) {
                            log.info("1.1.step137=>点击选择View");
                        }
                        WebElement distributeViewSelectElement = WebDriverUtils.expWaitForElement(manuDriver, By.xpath(manufacturingViewXPath), 10);
                        if (log.isInfoEnabled()) {
                            log.info("2.step112=>distributeViewSelectElement:" + distributeViewSelectElement.toString());
                        }
                        distributeViewSelectElement.click();

                        // 4.21 Choose SalesView
                        WebElement salesViewButtonElement = WebDriverUtils.expWaitForElement(manuDriver, By.xpath("//*[@id='dashboard-filter-viewFilter']//awsui-button-dropdown//button[1]"), 10);
                        if (log.isInfoEnabled()) {
                            log.info("1.step105=>salesViewButtonElement:" + salesViewButtonElement.toString());
                        }
                        salesViewButtonElement.click();

                        // 4.22 Select Shipped COGS
                        if (log.isInfoEnabled()) {
                            log.info("1.1.step137=>点击选择SalesView");
                        }
                        WebElement salesViewSelectElement = WebDriverUtils.expWaitForElement(manuDriver, By.xpath(salesViewShippedCOGSLevelXPath), 10);
                        if (log.isInfoEnabled()) {
                            log.info("2.step112=>salesViewSelectElement:" + salesViewSelectElement.toString());
                        }
                        salesViewSelectElement.click();

                        //4.3点击应用按钮
                        WebElement applyElement = WebDriverUtils.expWaitForElement(manuDriver, By.xpath("//*[@id='dashboard-filter-applyCancel']/div/awsui-button[2]/button"), 10);
                        applyElement.click();

                        sleep(10000);

                        // 6.抓取点击下载元素进行点击
                        // 判断是否出现了Download按钮,未在规定时间内出现重新刷新页面
                        WebElement downloadButtonElement = WebDriverUtils.expWaitForElement(manuDriver, By.xpath("//*[@id='downloadButton']//button[1]"), 30);
                        downloadButtonElement.click();

                        // 7.抓取CSV元素生成并进行点击
                        WebElement detailCsvDownloadButtonElement = WebDriverUtils.expWaitForElement(manuDriver, By.xpath(detailCsvXPath), 30);
                        detailCsvDownloadButtonElement.click();

                        // 8.获取点击之后的弹出框点击确定
                        if (log.isInfoEnabled()) {
                            log.info("1.step132=>wait for alert is present");
                        }

                        WebDriverUtils.waitAlert(manuDriver, 60);

                        if (log.isInfoEnabled()) {
                            log.info("1.1.step137=>scrapy the alert");
                        }
                        //获取弹出框
                        Alert downloadAlertElement = manuDriver.switchTo().alert();
                        //获取框中文本内容
                        log.info("alert text:" + downloadAlertElement.getText());
                        downloadAlertElement.accept();

                        sleep(10000);

                        log.info("[file download]");

                    }catch (Exception e){
                        log.info("[Get manufacturing view failed]", e);
                        manuDriver.quit();
                        throw e;
                    }finally {
                        manuDriver.quit();
                    }

                }
            });

            CompletableFuture<Void> sourcingViewCompletableFuture = CompletableFuture.runAsync(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    WebDriver sourcingDriver = WebDriverUtils.getWebDriver(downloadFilePath, false);
                    WebDriverWait sourcingWait =  new WebDriverWait(sourcingDriver, 60);

                    try{
                        sourcingDriver.navigate().to(SpiderUrl.AMAZON_VC_404);

                        WebDriverUtils.addSeleniumCookies(sourcingDriver, latestVCCookies);

                        //4.1 navigate to sales daily page
                        navigateToPage(sourcingDriver);

                        //4.1点击日期选择按钮, Reporting range
                        WebElement reportingRangeButtonElement = WebDriverUtils.expWaitForElement(sourcingDriver, By.xpath("//*[@id=\"dashboard-filter-reportingRange\"]//awsui-button-dropdown//awsui-button/button"), 20);
                        if (log.isInfoEnabled()) {
                            log.info("1.step105=>reportingRangeButtonElement:" + reportingRangeButtonElement.toString());
                        }
                        WebDriverUtils.elementClick(reportingRangeButtonElement);

                        //4.2点击选择daily
                        WebElement dailySelectElement = WebDriverUtils.expWaitForElement(sourcingDriver, By.xpath("//*[@id=\"dashboard-filter-reportingRange\"]/div/awsui-button-dropdown//ul/li[contains(@data-testid, 'DAILY')]"), 10);
                        if (log.isInfoEnabled()) {
                            log.info("2.step112=>dailySelectElement:" + dailySelectElement.toString());
                        }
                        dailySelectElement.click();

                        if(parseDate != null){
                            WebElement toDateEle = WebDriverUtils.expWaitForElement(sourcingDriver, By.xpath(dateRangeToDateXPath), 10);
                            toDateEle.click();

                            WebDriverUtils.randomSleep();

                            // select month
                            WebElement leftMonthEle = WebDriverUtils.expWaitForElement(sourcingDriver, By.xpath(datePickerLeftMonthXPath), 10);
                            String eleText = leftMonthEle.getText().toLowerCase();
                            String parseDateMonthStr = parseDate.getMonth().toString().toLowerCase();

                            while (!eleText.contains(parseDateMonthStr)){

                                WebElement previousEle = WebDriverUtils.expWaitForElement(sourcingDriver, By.xpath(datePickerPreviousBtnXPath), 10);

                                WebDriverUtils.elementClick(previousEle);

                                WebDriverUtils.randomSleep();

                                leftMonthEle = WebDriverUtils.expWaitForElement(sourcingDriver, By.xpath(datePickerLeftMonthXPath), 10);
                                eleText = leftMonthEle.getText().toLowerCase();
                            }

                            // select date
                            String leftDatePickerDateXPathFormat = "/html/body/div[4]/div/div[2]//div[@aria-label='day-%s']";
                            int day = parseDate.getDayOfMonth();
                            String dateXPath = String.format(leftDatePickerDateXPathFormat, day);
                            List<WebElement> elements = WebDriverUtils.expWaitForElements(sourcingDriver, By.xpath(dateXPath), 10);

                            WebElement dateEle = null;

                            if (elements.size() > 1){
                                if(day <= 7 ){
                                    dateEle = elements.get(0);
                                }else{
                                    dateEle = elements.get(1);
                                }
                            }else{
                                dateEle = elements.get(0);
                            }

                            WebDriverUtils.elementClick(dateEle);
                        }

                        // 4.21 Choose DistributeView View
                        WebElement distributeViewViewButtonElement = WebDriverUtils.expWaitForElement(sourcingDriver, By.xpath("//*[@id='dashboard-filter-distributorView']//awsui-button-dropdown//button"), 10);
                        if (log.isInfoEnabled()) {
                            log.info("1.step105=>distributeViewViewButtonElement:" + distributeViewViewButtonElement.toString());
                        }
                        distributeViewViewButtonElement.click();

                        // 4.22点击选择View
                        if (log.isInfoEnabled()) {
                            log.info("1.1.step137=>点击选择View");
                        }
                        WebElement distributeViewSelectElement = WebDriverUtils.expWaitForElement(sourcingDriver, By.xpath(sourcingViewXPath), 10);
                        if (log.isInfoEnabled()) {
                            log.info("2.step112=>distributeViewSelectElement:" + distributeViewSelectElement.toString());
                        }
                        distributeViewSelectElement.click();

                        // 4.21 Choose SalesView
                        WebElement salesViewButtonElement = WebDriverUtils.expWaitForElement(sourcingDriver, By.xpath("//*[@id='dashboard-filter-viewFilter']//awsui-button-dropdown//button[1]"), 10);
                        if (log.isInfoEnabled()) {
                            log.info("1.step105=>salesViewButtonElement:" + salesViewButtonElement.toString());
                        }
                        salesViewButtonElement.click();

                        // 4.22 Select Shipped COGS
                        if (log.isInfoEnabled()) {
                            log.info("1.1.step137=>点击选择SalesView");
                        }
                        WebElement salesViewSelectElement = WebDriverUtils.expWaitForElement(sourcingDriver, By.xpath(salesViewShippedCOGSLevelXPath), 10);
                        if (log.isInfoEnabled()) {
                            log.info("2.step112=>salesViewSelectElement:" + salesViewSelectElement.toString());
                        }
                        salesViewSelectElement.click();

                        //4.3点击应用按钮
                        WebElement applyElement = WebDriverUtils.expWaitForElement(sourcingDriver, By.xpath("//*[@id='dashboard-filter-applyCancel']/div/awsui-button[2]/button"), 10);
                        applyElement.click();

                        sleep(10000);

                        // 6.抓取点击下载元素进行点击
                        // 判断是否出现了Download按钮,未在规定时间内出现重新刷新页面
                        WebElement downloadButtonElement = WebDriverUtils.expWaitForElement(sourcingDriver, By.xpath("//*[@id='downloadButton']//button[1]"), 10);
                        downloadButtonElement.click();

                        // 7.抓取CSV元素生成并进行点击
                        WebElement detailCsvDownloadButtonElement = WebDriverUtils.expWaitForElement(sourcingDriver, By.xpath(detailCsvXPath), 10);
                        detailCsvDownloadButtonElement.click();

                        // 8.获取点击之后的弹出框点击确定
                        if (log.isInfoEnabled()) {
                            log.info("1.step132=>wait for alert is present");
                        }

                        sourcingWait.until(ExpectedConditions.alertIsPresent());
                        if (log.isInfoEnabled()) {
                            log.info("1.1.step137=>scrapy the alert");
                        }
                        Alert downloadAlertElement = sourcingDriver.switchTo().alert();//获取弹出框
                        log.info("alert text:" + downloadAlertElement.getText());//获取框中文本内容
                        log.info("alert toString():" + downloadAlertElement.toString());
                        downloadAlertElement.accept();

                        sleep(10000);

                        log.info("[file download]");

                    }catch (Exception e){
                        sourcingDriver.quit();
                        log.error("[Get sourcing view failed]", e);
                        throw e;
                    }finally {
                        sourcingDriver.quit();
                    }
                }
            });

            CompletableFuture.allOf(manufacturingViewCompletableFuture, sourcingViewCompletableFuture).join();

            try {
                sleep(30000);
            } catch (InterruptedException e) {
                throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
            }

        } catch (Exception e) {
            driver.quit();
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(),RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
        } finally {
            driver.quit();
        }

        if (log.isInfoEnabled()) {
            log.info("1.step84=>抓取结束");
        }

    }

    /**
     * Navigate driver to Daily Sales page
     * @param driver
     * @throws InterruptedException
     */
    private void navigateToPage(WebDriver driver) throws InterruptedException {

        // 1.1设置页面超时等待时间,20S
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

        // 2.Navigate to daily sales page
        driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
        driver.get(SpiderUrl.AMAZON_VC_ANALYTICS_SALES_DIAGNOSTIC);

        sleep(3000);

        if(driver.getCurrentUrl().equals(SpiderUrl.AMAZON_VC_ANALYTICS_SALES_DIAGNOSTIC)){
            // 4.重定向跳转
            driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
            driver.get(SpiderUrl.AMAZON_VC_DASHBOARD);

            sleep(3000);

            //4.0 click salesDiagnostic
            WebElement inventoryHealthButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//a[contains(@href,'/analytics/dashboard/salesDiagnostic')]"), 10);
            if (log.isInfoEnabled() && inventoryHealthButtonElement != null) {
                log.info("1.step105=>reportingRangeButtonElement:" + inventoryHealthButtonElement.toString());
            }
            WebDriverUtils.elementClick(inventoryHealthButtonElement);
            sleep(5000);
        }

    }

}

