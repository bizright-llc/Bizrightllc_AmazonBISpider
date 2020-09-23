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
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

/**
 * Amazon每日仓库库存数据抓取
 */
@Component
@Slf4j
public class AmazonVcDailyInventoryHealthManufacturing implements PageProcessor {

    int webDriverWaitSecond = 60;

    private SpiderConfig spiderConfig;

    private CommonSettingService commonSettingService;

    @Autowired
    public AmazonVcDailyInventoryHealthManufacturing(SpiderConfig spiderConfig, CommonSettingService commonSettingService) {
        this.spiderConfig = spiderConfig;
        this.commonSettingService = commonSettingService;
    }

    private Site site = Site
            .me()
            .setRetryTimes(3)
            .setDomain("https://vendorcentral.amazon.com/analytics/dashboard/inventoryHealth")
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

        // 1.建立WebDriver
        System.setProperty("webdriver.chrome.driver", spiderConfig.getChromeDriverPath());

        try {

            // Create two threads:
            Thread thread1 = new Thread() {
                public void run() {
                    getManufacturingFile(spiderConfig, webDriverWaitSecond);
                }
            };


            // Start the downloads.
            thread1.start();

            // Wait for them both to finish
            thread1.join();

        } catch (Exception e) {


            e.printStackTrace();
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
        }

        if (log.isInfoEnabled()) {
            log.info("1.step84=>抓取结束");
        }

    }

    /**
     * Navigate to analytics inventory health page
     * @param driver
     * @param wait
     * @throws InterruptedException
     */
    private void navigateToPage(WebDriver driver, WebDriverWait wait) throws InterruptedException {

        // 1.1设置页面超时等待时间,20S
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

        // 2.Navigate to inventory health page
        driver.get(SpiderUrl.AMAZON_VC_ANALYTICS_INVENTORY_HEALTH);

        sleep(3000);

        if(!driver.getCurrentUrl().equals(SpiderUrl.AMAZON_VC_ANALYTICS_INVENTORY_HEALTH)){
            // 4.重定向跳转
            driver.navigate().to(SpiderUrl.AMAZON_VC_DASHBOARD);

            //4.0 click inventoryHealth
            WebElement inventoryHealthButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//a[contains(@href,'/analytics/dashboard/inventory')]"), 10);
            if (log.isInfoEnabled() && inventoryHealthButtonElement != null) {
                log.info("1.step105=>reportingRangeButtonElement:" + inventoryHealthButtonElement.toString());
            }
            WebDriverUtils.elementClick(inventoryHealthButtonElement);
            sleep(10000);
        }

    }

    /**
     *
     */
    private void getManufacturingFile(SpiderConfig spiderConfig, int webDriverWaitSecond) {

        String filePath = spiderConfig.getDownloadPath();

        WebDriver driver = WebDriverUtils.getWebDriver(filePath, false);

        ObjectMapper objectMapper = new ObjectMapper();

        try {

            WebDriverWait wait = new WebDriverWait(driver, webDriverWaitSecond);

            // 2.初始打开页面
            driver.navigate().to(SpiderUrl.AMAZON_VC_INDEX);

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

            navigateToPage(driver, wait);

            //4.1点击日期选择按钮
            WebElement reportingRangeButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-reportingRange']//awsui-button-dropdown//button[1]"), 10);
            if (log.isInfoEnabled()) {
                log.info("1.step105=>reportingRangeButtonElement:" + reportingRangeButtonElement.toString());
            }
            WebDriverUtils.elementClick(reportingRangeButtonElement);

            //4.2点击选择daily
            driver.manage().timeouts().pageLoadTimeout(7, TimeUnit.SECONDS); // 页面加载超时时间
            if (log.isInfoEnabled()) {
                log.info("1.1.step137=>点击选择daily");
            }

            WebElement dailySelectElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"dashboard-filter-reportingRange\"]/div/awsui-button-dropdown/div/div/ul/li[1]"), 10);
            if (log.isInfoEnabled() && dailySelectElement != null) {
                log.info("2.step112=>dailySelectElement:" + dailySelectElement.toString());
            }
            WebDriverUtils.elementClick(dailySelectElement);

            // 4.21点击DistributeView View
            WebElement distributeViewViewButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-distributorView']//awsui-button-dropdown//button"), 10);
            if (log.isInfoEnabled() && distributeViewViewButtonElement != null) {
                log.info("1.step105=>distributeViewViewButtonElement:" + distributeViewViewButtonElement.toString());
            }
            WebDriverUtils.elementClick(distributeViewViewButtonElement);
//            distributeViewViewButtonElement.click();

            // 4.22点击选择View
            if (log.isInfoEnabled()) {
                log.info("1.1.step137=>点击选择 Manufacturing View");
            }
            WebElement distributeViewSelectElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"dashboard-filter-distributorView\"]/div/awsui-button-dropdown/div/div/ul/li[1]"), 10);
            if (log.isInfoEnabled() && distributeViewSelectElement != null) {
                log.info("2.step112=>distributeViewSelectElement:" + distributeViewSelectElement.toString());
            }
            WebDriverUtils.elementClick(distributeViewSelectElement);

            //4.3点击应用按钮
            sleep(7000);
            driver.manage().timeouts().pageLoadTimeout(7, TimeUnit.SECONDS); // 页面加载超时时间
            WebElement applyElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-applyCancel']/div/awsui-button[2]/button"), 10);
            WebDriverUtils.elementClick(applyElement);

            // 5.进行操作点击下载Excel,抓取标题
//            WebElement titleElement = driver.findElement(By.xpath("//title"));
//            String title = titleElement.getAttribute("text");

            // 6.抓取点击下载元素进行点击
            // 判断是否出现了Download按钮,未在规定时间内出现重新刷新页面
            driver.manage().timeouts().pageLoadTimeout(7, TimeUnit.SECONDS); // 页面加载超时时间
            // css selector click
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"downloadButton\"]/awsui-button-dropdown/div/awsui-button/button")));

            WebElement downloadButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"downloadButton\"]/awsui-button-dropdown/div/awsui-button/button"), 10);
            WebDriverUtils.elementClick(downloadButtonElement);

            // 7.抓取CSV元素生成并进行点击
            WebElement csvButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"downloadButton\"]/awsui-button-dropdown/div/div/ul/li/ul/li[2]"), 10);
            WebDriverUtils.elementClick(csvButtonElement);

            // 8.获取点击之后的弹出框点击确定
            if (log.isInfoEnabled()) {
                log.info("1.step132=>wait for alert is present");
            }
//        WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.alertIsPresent());
            if (log.isInfoEnabled()) {
                log.info("1.1.step137=>scrapy the alert");
            }
            Alert downloadAlertElement = driver.switchTo().alert();//获取弹出框
            log.info("alert text:" + downloadAlertElement.getText());//获取框中文本内容
            log.info("alert toString():" + downloadAlertElement.toString());
            downloadAlertElement.accept();

//            String manufaturingFilename = "InventoryHealthManufacturing";

            sleep(3000);

            // 更新下载文件名
//            FileUtil.rename(new File(filePath + filename + ".csv"), StrUtil.concat(true, manufaturingFilename, "-", DateUtil.format(DateUtil.offsetDay(DateUtil.date(), 0), DateFormat.YEAR_MONTH_DAY)), true, true);

            log.info("Download Inventory Health Manufacturing Success");

        } catch (Exception ex) {

            driver.quit();

            log.error("Get Inventory Health Manufacturing failed", ex);

        } finally {
            driver.quit();
        }

    }

}

