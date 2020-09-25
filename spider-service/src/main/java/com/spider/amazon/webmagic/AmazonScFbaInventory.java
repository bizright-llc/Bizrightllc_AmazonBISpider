package com.spider.amazon.webmagic;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.entity.Cookie;
import com.spider.amazon.remote.api.SpiderUrl;
import com.spider.amazon.utils.JsonToListUtil;
import com.spider.amazon.utils.WebDriverUtils;
import lombok.extern.slf4j.Slf4j;
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

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

/**
 * Amazon卖家中心每周BuyBox数据抓取
 */
@Component
@Slf4j
public class AmazonScFbaInventory implements PageProcessor {

    //    private static final String jsonPathSc = "C:\\Program Files\\Java\\BiSpider\\cookieSc.json";
//    local test
//    private static final String jsonPathSc = "/Users/shaochinlin/Documents/Bizright/BI/BiSpider/cookieSc.json";

    private static final int offerSetDay = 0;

//    local test
//    private static final String filePath = "C:\\Users\\paulin.f\\Downloads\\";
//    private static final String filePath = "/Users/shaochinlin/Downloads/bizright-spider/";
//    private static final String filePath = "/Users/shaochinlin/Downloads/";

    private static final String newFileName = "Fba_Inventory";

    private SpiderConfig spiderConfig;

    @Autowired
    public AmazonScFbaInventory(SpiderConfig spiderConfig) {
        this.spiderConfig = spiderConfig;
    }

//    @Autowired
//    private CookiesUtils cookiesUtils;

//    @Value("${amazon.sc.freelogin.cookies.name}")
//    private String cookiesConfigName;

    private Site site = Site
            .me()
            .setRetryTimes(3)
            .setDomain(SpiderUrl.SPIDER_SC_INDEX)
            .setSleepTime(3000)
            .setUserAgent(
                    "User-Agent:Mozilla/5.0(Macintosh;IntelMacOSX10_7_0)AppleWebKit/535.11(KHTML,likeGecko)Chrome/17.0.963.56Safari/535.11");

    /**
     * 设置网站信息
     *
     * @return
     */
    @Override
    public Site getSite() {
        return site;
    }

    /**
     * 页面抓取过程
     *
     * @param page page
     */
    @Override
    public void process(Page page) {
        if (log.isInfoEnabled()) {
            log.info("0.step21=>进入抓取");
        }


        // 1.建立WebDriver
        System.setProperty("webdriver.chrome.driver", spiderConfig.getChromeDriverPath());
        WebDriver driver = null;

        String filePath = spiderConfig.getDownloadPath();

        driver = WebDriverUtils.getWebDriver(spiderConfig.getChromeDriverPath(), filePath, true);

        String filename = "";

        try {

            // 1.0隐式等待对象声明
            WebDriverWait wait = new WebDriverWait(driver, 30);

            // 1.1设置页面超时等待时间,20S
            driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

            // 2.初始打开页面
            driver.get(SpiderUrl.SPIDER_SC_INDEX);

            // 3.add Cookies 在工具类中解析json
            driver.manage().deleteAllCookies();
            List<Cookie> listCookies = JsonToListUtil.amazonSourceCookieList2CookieList(JsonToListUtil.getListByPath(spiderConfig.getAmzScCookieFilepath()));
            for (Cookie cookie : listCookies) {
                // Cookie(String name, String value, String domain, String path, Date expiry, boolean isSecure, boolean isHttpOnly)
                if (!cookie.getName().equals("__Host-mons-selections") && !cookie.getName().equals("__Host-mselc")) {
                    driver.manage().addCookie(new org.openqa.selenium.Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(),
                            cookie.getPath(), cookie.getExpiry(), cookie.getIsSecure(), cookie.getIsHttpOnly()));
                }
            }


            // 4.重定向跳转
            driver.get(SpiderUrl.SPIDER_SC_FBAINV);

//            // 获得cookie
//            Set<org.openqa.selenium.Cookie> coo = driver.manage().getCookies();
//            System.out.println(coo);

            // 4.show All按钮点击
//            WebElement showAllButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='sc-sidepanel']/div/ul[2]/li[contains(@class,'show-more')]/a[1]"), 10);
//            showAllButtonElement.click();
            // css selector click
            driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS); // 页面加载超时时间
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='sc-sidepanel']/div/ul[2]/li[contains(@class,'show-more')]/a[1]"))).click();
            sleep(5000);

            // 5.跳转Manage FBA Inventory页面
//            WebElement redirectButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='FBA_MYI_UNSUPPRESSED_INVENTORY']"), 10);
//            redirectButtonElement.click();
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='FBA_MYI_UNSUPPRESSED_INVENTORY']"))).click();
            sleep(10000);

            // 6.点击请求下载FBA Inventory数据按钮
//            WebElement requestDownloadButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='requestCsvTsvDownload']//button[@name='Request .csv Download']"), 10);
//            requestDownloadButtonElement.click();
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='requestCsvTsvDownload']//button[@name='Request .csv Download']"))).click();
            sleep(10000);

            // 7.点击下载按钮
            String curdate = DateUtil.format(DateUtil.offsetDay(DateUtil.date(), offerSetDay), DateFormat.YEAR_MONTH_DAY_Mdyy1);
            WebElement downloadButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='downloadArchive']/table/tbody/tr[1]/td[contains(text(),'" + curdate + "')]/..//a"), 300);
            String reportPath = downloadButtonElement.getAttribute("href");
            // example: https://sellercentral.amazon.com/gp/ssof/reports/documents/_GET_FBA_MYI_UNSUPPRESSED_INVENTORY_DATA__17683055807018222.txt?ie=UTF8&contentType=text%2Fcsv
            filename = StrUtil.subBetween(reportPath, "DATA__", ".txt");
            log.info("reportPath:" + reportPath + " filename:" + filename);
            downloadButtonElement.click();

            try {
                sleep(30000);
            } catch (InterruptedException e) {
                throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
        } finally {
            driver.quit();
        }

        // 更新下载文件名
        FileUtil.rename(new File(filePath + filename + ".csv"), StrUtil.concat(true, newFileName, "-", DateUtil.format(DateUtil.offsetDay(DateUtil.date(), offerSetDay), DateFormat.YEAR_MONTH_DAY)), true, true);

        if (log.isInfoEnabled()) {
            log.info("1.step84=>抓取结束");
        }

    }

    public static void main(String[] args) {
        System.out.println("0.step67=>抓取程序开启。");

        Spider.create(new AmazonScFbaInventory(null))
                .addUrl(SpiderUrl.SPIDER_SC_INDEX)
                .run();

        System.out.println("end.step93=>抓取程序结束。");

    }

}

