package com.spider.amazon.webmagic;

import com.common.exception.ServiceException;
import com.spider.amazon.cons.DriverPathCons;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.entity.Cookie;
import com.spider.amazon.utils.CookiesUtils;
import com.spider.amazon.utils.JsonToListUtil;
import com.spider.amazon.utils.WebDriverUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

/**
 * Amazon每日仓库库存数据抓取
 */
@Component
@Slf4j
public class AmazonVcDailyInventoryHealth implements PageProcessor {

    @Autowired
    private CookiesUtils cookiesUtils;

    @Value("${amazon.vc.freelogin.cookies.name}")
    private String cookiesConfigName;

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
//        Set<Cookie> cookies = cookiesUtils.keyValueCookies2CookiesSet(cookiesConfigName, ";", "=");
        Set<Cookie> cookies = cookiesUtils.keyValueCookies2CookiesSet("amazon.vc.freelogin.cookies", ";", "=");

        for (Cookie cookie : cookies) {
            site.addCookie(cookie.getName().toString(), cookie.getValue().toString());
        }
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
        WebDriver driver = new ChromeDriver();

        try {

            // 1.0隐式等待对象声明
            WebDriverWait wait = new WebDriverWait(driver, 10);

            // 1.1设置页面超时等待时间,20S
            driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

            // 2.初始打开页面
            driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
            driver.get("https://www.google.com");


            // 3.add Cookies 在工具类中解析json
            driver.manage().deleteAllCookies();
            List<Cookie> listCookies = JsonToListUtil.amazonSourceCookieList2CookieList(JsonToListUtil.getList());
            for (Cookie cookie : listCookies) {
                // Cookie(String name, String value, String domain, String path, Date expiry, boolean isSecure, boolean isHttpOnly)
                driver.manage().addCookie(new org.openqa.selenium.Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(),
                        cookie.getPath(), cookie.getExpiry(), cookie.getIsSecure(), cookie.getIsHttpOnly()));
            }


            // 4.重定向跳转
            driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
            driver.get("https://vendorcentral.amazon.com/analytics/dashboard/inventoryHealth");


//        // 获得cookie
//        Set<org.openqa.selenium.Cookie> coo = driver.manage().getCookies();
//        System.out.println(coo);

            sleep(10000);

            //4.1点击日期选择按钮
            WebElement reportingRangeButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-reportingRange']//awsui-button-dropdown//button[1]"), 10);
            if (log.isInfoEnabled()) {
                log.info("1.step105=>reportingRangeButtonElement:" + reportingRangeButtonElement.toString());
            }
            reportingRangeButtonElement.click();

            //4.2点击选择daily
            driver.manage().timeouts().pageLoadTimeout(7, TimeUnit.SECONDS); // 页面加载超时时间
            if (log.isInfoEnabled()) {
                log.info("1.1.step137=>点击选择daily");
            }
            WebElement dailySelectElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-reportingRange']//awsui-button-dropdown//ul/li[1]/a"), 10);
            if (log.isInfoEnabled()) {
                log.info("2.step112=>dailySelectElement:" + dailySelectElement.toString());
            }
            dailySelectElement.click();

            // 4.21点击DistributeView View
            WebElement distributeViewViewButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-distributorView']//awsui-button-dropdown//button"), 10);
            if (log.isInfoEnabled()) {
                log.info("1.step105=>distributeViewViewButtonElement:" + distributeViewViewButtonElement.toString());
            }
            distributeViewViewButtonElement.click();

            // 4.22点击选择View
            if (log.isInfoEnabled()) {
                log.info("1.1.step137=>点击选择View");
            }
            WebElement distributeViewSelectElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-distributorView']//awsui-button-dropdown//ul/li[2]/a"), 10);
            if (log.isInfoEnabled()) {
                log.info("2.step112=>distributeViewSelectElement:" + distributeViewSelectElement.toString());
            }
            distributeViewSelectElement.click();


            //4.3点击应用按钮
            sleep(7000);
            driver.manage().timeouts().pageLoadTimeout(7, TimeUnit.SECONDS); // 页面加载超时时间
            WebElement applyElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='dashboard-filter-applyCancel']/div/awsui-button[2]/button"), 10);
            applyElement.click();

            // 5.进行操作点击下载Excel,抓取标题
            WebElement titleElement = driver.findElement(By.xpath("//title"));
            String title = titleElement.getAttribute("text");

            // 6.抓取点击下载元素进行点击
            // 判断是否出现了Download按钮,未在规定时间内出现重新刷新页面
            driver.manage().timeouts().pageLoadTimeout(7, TimeUnit.SECONDS); // 页面加载超时时间
            // css selector click
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='downloadButton']/awsui-button-dropdown/div/button"))).click();
            // xpath selector click  : xpath 选中无法点击,css加载的点击事件,元素加载完了,但是点击事件还未加载完
//        WebElement downloadButtonElement = WebDriverUtils.expWaitForElement(driver,By.xpath("//*[@id='downloadButton']/awsui-button-dropdown/div/button"),10);
//        if (log.isInfoEnabled()) {
//            log.info("6.step131=>downloadButtonElement:"+downloadButtonElement.getLocation());
//            log.info("6.step131=>downloadButtonElement text:"+downloadButtonElement.getText());
//        }
//        downloadButtonElement.click();

            // 7.抓取CSV元素生成并进行点击
            driver.manage().timeouts().pageLoadTimeout(7, TimeUnit.SECONDS); // 页面加载超时时间
            WebElement detailCsvDownloadButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='downloadButton']/awsui-button-dropdown//ul/li/ul/li[2]/a"), 10);
            detailCsvDownloadButtonElement.click();


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

            try {
                sleep(30000);
            } catch (InterruptedException e) {
                throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(),RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(),RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
        } finally {
            driver.quit();
        }

        if (log.isInfoEnabled()) {
            log.info("1.step84=>抓取结束");
        }

    }

    public static void main(String[] args) {
        System.out.println("0.step67=>抓取程序开启。");

        Spider.create(new AmazonVcDailyInventoryHealth())
                .addUrl("http://www.google.com")
                .run();

        System.out.println("end.step93=>抓取程序结束。");

    }


}

