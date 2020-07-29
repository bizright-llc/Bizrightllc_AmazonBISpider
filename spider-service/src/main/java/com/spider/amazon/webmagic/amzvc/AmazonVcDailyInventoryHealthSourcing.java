package com.spider.amazon.webmagic.amzvc;

import com.common.exception.ServiceException;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.cons.DriverPathCons;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.entity.Cookie;
import com.spider.amazon.utils.JsonToListUtil;
import com.spider.amazon.utils.WebDriverUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
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

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

/**
 * Amazon每日仓库库存数据抓取
 * Get Distribute View Sourcing
 */
@Component
@Slf4j
public class AmazonVcDailyInventoryHealthSourcing implements PageProcessor {

    int webDriverWaitSecond = 60;

    private SpiderConfig spiderConfig;

    @Autowired
    public AmazonVcDailyInventoryHealthSourcing(SpiderConfig spiderConfig) {
        this.spiderConfig = spiderConfig;
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
        System.setProperty("webdriver.chrome.driver", DriverPathCons.CHROME_DRIVER_PATH);

        try {

            // Create two threads:
            Thread thread2 = new Thread() {
                public void run() {
                    getSourcingFile(spiderConfig, webDriverWaitSecond);
                }
            };

            // Start the downloads.
            thread2.start();

            // Wait for them both to finish
            thread2.join();

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
        }

        if (log.isInfoEnabled()) {
            log.info("1.step84=>抓取结束");
        }

    }

    private void navigateToPage(WebDriver driver, WebDriverWait wait) throws InterruptedException {

        // 1.1设置页面超时等待时间,20S
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

        // 2.初始打开页面
        driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
        driver.get("https://www.google.com");


        // 3.add Cookies 在工具类中解析json
        driver.manage().deleteAllCookies();
        List<Cookie> listCookies = JsonToListUtil.amazonSourceCookieList2CookieList(JsonToListUtil.getListByPath(spiderConfig.getAmzVcCookieFilepath()));

        WebDriverUtils.addCookies(driver, listCookies);

        // 4.重定向跳转
        driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
        driver.get("https://vendorcentral.amazon.com/analytics/dashboard");

        sleep(10000);

        //4.0 click inventoryHealth
        WebElement inventoryHealthButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//span[1]/a[contains(@data-reactid,'inventoryHealth')]"), 10);
        if (log.isInfoEnabled() && inventoryHealthButtonElement != null) {
            log.info("1.step105=>reportingRangeButtonElement:" + inventoryHealthButtonElement.toString());
        }
        WebDriverUtils.elementClick(inventoryHealthButtonElement);
        sleep(10000);

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
    }

    /**
     *
     */
    private void getSourcingFile(SpiderConfig spiderConfig, int webDriverWaitSecond) {

        String downloadFilePath = spiderConfig.getDownloadPath();

        WebDriver driver = WebDriverUtils.getWebDriver(downloadFilePath);

        try {

            WebDriverWait wait = new WebDriverWait(driver, webDriverWaitSecond);

            navigateToPage(driver, wait);

            // 4.21点击DistributeView View
            WebElement distributeViewViewButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-distributorView']//awsui-button-dropdown//button"), 10);
            if (log.isInfoEnabled() && distributeViewViewButtonElement != null) {
                log.info("1.step105=>distributeViewViewButtonElement:" + distributeViewViewButtonElement.toString());
            }
            WebDriverUtils.elementClick(distributeViewViewButtonElement);
//            distributeViewViewButtonElement.click();

            // 4.22点击选择 Sourcing View
            if (log.isInfoEnabled()) {
                log.info("1.1.step137=>点击选择 Sourcing View");
            }
            WebElement distributeViewSelectElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"dashboard-filter-distributorView\"]/div/awsui-button-dropdown/div/div/ul/li[2]"), 10);
            if (log.isInfoEnabled() && distributeViewSelectElement != null) {
                log.info("2.step112=>distributeViewSelectElement:" + distributeViewSelectElement.toString());
            }
            WebDriverUtils.elementClick(distributeViewSelectElement);

            //4.3点击应用按钮
            sleep(7000);
            driver.manage().timeouts().pageLoadTimeout(7, TimeUnit.SECONDS); // 页面加载超时时间
            WebElement applyElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-applyCancel']/div/awsui-button[2]/button"), 10);
            WebDriverUtils.elementClick(applyElement);

            sleep(5000);

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

            sleep(5000);
//
//            File file = FileUtils.getLatestFileWithNameFromDir(downloadFilePath, downloadFilename);
//
//            file.renameTo(new File(downloadFilePath + newFilename));
//
//            log.info("Rename file to " + newFilename);

            try {
                sleep(30000);
            } catch (InterruptedException e) {
                throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
            }

        } catch (Exception ex) {

            ex.printStackTrace();

            log.error("Get Inventory Health Sourcing failed");

            log.error(ex.getLocalizedMessage());

        } finally {
            driver.quit();
        }

    }

    public static void main(String[] args) {


    }

}
