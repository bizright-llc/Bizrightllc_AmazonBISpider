package com.spider.amazon.webmagic;

import cn.hutool.core.date.DateUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.cons.DriverPathCons;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.entity.Cookie;
import com.spider.amazon.remote.api.SpiderUrl;
import com.spider.amazon.utils.CookiesUtils;
import com.spider.amazon.utils.JsonToListUtil;
import com.spider.amazon.utils.UsDateUtils;
import com.spider.amazon.utils.WebDriverUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

/**
 * Amazon卖家中心每周BuyBox数据抓取
 */
@Component
@Slf4j
public class AmazonScBuyBox implements PageProcessor {

    private static final String jsonPathSc = "C:\\Program Files\\Java\\BiSpider\\cookieSc.json";

    @Autowired
    private CookiesUtils cookiesUtils;

    @Value("${amazon.sc.freelogin.cookies.name}")
    private String cookiesConfigName;

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
        WebDriver driver = new ChromeDriver();

        try {

            // 1.1设置页面超时等待时间,20S
            driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

            // 2.初始打开页面
            driver.get(SpiderUrl.SPIDER_SC_INDEX);

            // 3.add Cookies 在工具类中解析json
            driver.manage().deleteAllCookies();
            List<Cookie> listCookies = JsonToListUtil.amazonSourceCookieList2CookieList(JsonToListUtil.getListByPath(jsonPathSc));
            for (Cookie cookie : listCookies) {
                // Cookie(String name, String value, String domain, String path, Date expiry, boolean isSecure, boolean isHttpOnly)
                if (!cookie.getName().equals("__Host-mons-selections")) {
                    driver.manage().addCookie(new org.openqa.selenium.Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(),
                            cookie.getPath(), cookie.getExpiry(), cookie.getIsSecure(), cookie.getIsHttpOnly()));
                }
            }


            // 4.重定向跳转
            // 构造查询日期数据，获取上一个自然周的数据
            String fromDate = DateUtil.format(UsDateUtils.beginOfWeek(DateUtil.lastWeek()), DateFormat.YEAR_MONTH_DAY_MMddyyyy);
            String toDate = DateUtil.format(UsDateUtils.endOfWeek(DateUtil.lastWeek()), DateFormat.YEAR_MONTH_DAY_MMddyyyy);
            String filterFromDate = fromDate;
            String filterToDate = toDate;
            final String redirectUrl = SpiderUrl.SPIDER_SC_BUYBOX.replace("{filterFromDate}", filterFromDate)
                    .replace("{filterToDate}", filterToDate)
                    .replace("{fromDate}", fromDate)
                    .replace("{toDate}", toDate);
            driver.get(redirectUrl);

            sleep(10000);

//            // 获得cookie
//            Set<org.openqa.selenium.Cookie> coo = driver.manage().getCookies();
//            System.out.println(coo);


            // 5.进行操作点击下载Excel,抓取标题
            WebElement titleElement = driver.findElement(By.xpath("//title"));
            String title = titleElement.getAttribute("text");

            // 6.抓取点击下载元素进行点击
            // 判断是否出现了Download按钮,未在规定时间内出现重新刷新页面
            WebElement downloadButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='export']"), 10);
            downloadButtonElement.click();

            // 7.抓取CSV元素生成并进行点击
            WebElement detailCsvDownloadButtonElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='downloadCSV']"), 10);
            detailCsvDownloadButtonElement.click();

            try {
                sleep(30000);
            } catch (InterruptedException e) {
                throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
            }
        } catch (Exception e) {
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
        } finally {
            driver.quit();
        }

        if (log.isInfoEnabled()) {
            log.info("1.step84=>抓取结束");
        }

    }

    public static void main(String[] args) {
        System.out.println("0.step67=>抓取程序开启。");

        Spider.create(new AmazonScBuyBox())
                .addUrl(SpiderUrl.SPIDER_SC_INDEX)
                .run();

        System.out.println("end.step93=>抓取程序结束。");

    }

}

