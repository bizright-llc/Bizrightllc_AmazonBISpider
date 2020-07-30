package com.spider.amazon.webmagic.amzvc;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.cons.DriverPathCons;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.entity.Cookie;
import com.spider.amazon.model.VcPromotionInfoDO;
import com.spider.amazon.model.VcPromotionProductInfoDO;
import com.spider.amazon.remote.api.SpiderUrl;
import com.spider.amazon.utils.ConvertUtils;
import com.spider.amazon.utils.JsonToListUtil;
import com.spider.amazon.utils.WebDriverUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.hutool.core.thread.ThreadUtil.sleep;

/**
 * Amazon 买家中心Promotions抓取
 * TODO Promotion详情目前暂未支持单个Promotion10个以上产品的完整抓取
 * TODO 该版本是正常自动化跳转流程爬取
 */
@Component
@Slf4j
public class AmazonVcPromotionsProcessor implements PageProcessor {

    private SpiderConfig spiderConfig;

    @Autowired
    public AmazonVcPromotionsProcessor(SpiderConfig spiderConfig) {
        this.spiderConfig = spiderConfig;
    }

    private Site site = Site
            .me()
            .setRetryTimes(3)
            .setDomain("https://vendorcentral.amazon.com/hz/vendor/members/promotions/list/home?ref_=vc_xx_subNav")
            .setSleepTime(3000)
            .setUserAgent("User-Agent:Mozilla/5.0(Macintosh;IntelMacOSX10_7_0)AppleWebKit/535.11(KHTML,likeGecko)Chrome/17.0.963.56Safari/535.11");

    /**
     * 设置网站信息
     *
     * @return
     */
    public Site getSite() {

        List<Cookie> listCookies = JsonToListUtil.amazonSourceCookieList2CookieList(JsonToListUtil.getList());

        for (Cookie cookie : listCookies) {
            site.addCookie(cookie.getName(), cookie.getValue());
        }

        return site;
    }

    /**
     * 爬取主逻辑
     *
     * @param page page
     */
    public void process(Page page) {

        log.info("Extras:[{}]", page.getRequest().getExtras());

        log.info("抓取主逻辑");
        if (page.getUrl().regex(SpiderUrl.SPIDER_INDEX).match()) {
            // 列表页
            processMain(page);
        } else { // 详情页
            processDetail(page);
        }

    }


    /**
     * 页面抓取过程
     *
     * @param page page
     */
    private void processMain(Page page) {
        if (log.isInfoEnabled()) {
            log.info("0.step21=>进入抓取");
        }

        // 0.参数值
        String crawId = DateUtil.format(DateUtil.date(), DateFormat.YEAR_MONTH_DAY_yyyyMMddHHmmss1);

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
            driver.get("https://www.google.com/");

            // 3.add Cookies 在工具类中解析json
            driver.manage().deleteAllCookies();

            WebDriverUtils.addCookies(driver, JsonToListUtil.amazonSourceCookieList2CookieList(JsonToListUtil.getListByPath(spiderConfig.getAmzVcCookieFilepath())));

//            List<Cookie> listCookies = JsonToListUtil.amazonSourceCookieList2CookieList(JsonToListUtil.getList());
//            for (Cookie cookie : listCookies) {
//                // Cookie(String name, String value, String domain, String path, Date expiry, boolean isSecure, boolean isHttpOnly)
//                driver.manage().addCookie(new org.openqa.selenium.Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(),
//                        cookie.getPath(), cookie.getExpiry(), cookie.getIsSecure(), cookie.getIsHttpOnly()));
//            }

            // 4.重定向跳转
            driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
            driver.get("https://vendorcentral.amazon.com/hz/vendor/members/promotions/list/home?ref_=vc_xx_subNav");


            // 4.1 点击页记录数按钮
            WebElement recNumEle = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='a-autoid-1-announce']"), 10);
            recNumEle.click();

            // 4.2 选择最大记录数
            WebElement maxNumEle = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-list-record-per-page-drop-down_2']"), 10);
            maxNumEle.click();


            // 4.3 Search the promotion end date not before today
            LocalDate today = LocalDate.now();
            today.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));

            String todayStr = today.toString();

            // promotion date end date
            WebElement endDateAfterEle = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"endDateAfter\"]"), 10);
            endDateAfterEle.sendKeys(todayStr);
            WebDriverUtils.waitForLoad(driver);

            int pageIndex = 0;
            do {
                pageIndex++;

                log.info("page [{}]", pageIndex);
                WebDriverUtils.waitForLoad(driver);
                if (pageIndex != 1) {

                    String nextEleXPath = "//*[@id='promotion-list-pagination']/ul/li[contains(@class,'a-last')]/a";
                    String overlayXPath = "//div[@class='mt-loading-overlay']";
                    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(overlayXPath)));
                    WebElement nextEle = WebDriverUtils.expWaitForElement(driver, By.xpath(nextEleXPath), 10);
//                    ExpectedConditions.elementToBeClickable(nextEle);
//                    nextEle.click();
                    if (nextEle != null && nextEle.isEnabled()) {
                        WebDriverUtils.elementClick(nextEle);
                    } else {
                        break;
                    }
                }

                // 5.获取页面链接
                WebElement tableElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-list']//table"), 20);
                List<WebElement> links = tableElement.findElements(By.xpath("//tr[@id!='head-row']//a"));
                for (int linksIndex = 0; linksIndex < links.size(); linksIndex++) {
                    log.info("link=> [{}]", links.get(linksIndex).getAttribute("href"));
                    Request request = new Request(links.get(linksIndex).getAttribute("href"));
                    request.putExtra("craw_id", crawId);
                    page.addTargetRequest(request);
                }
            } while (WebDriverUtils.isExistsElementFindByXpath(driver, By.xpath("//*[@id='promotion-list-pagination']/ul/li[contains(@class,'a-last')]/a"), 20));

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
        } finally {
            driver.close();
        }

        if (log.isInfoEnabled()) {
            log.info("1.step84=>抓取结束");
        }

    }

    /**
     * 详情页面爬取
     *
     * @param page
     */
    private void processDetail(Page page) {
        log.info("［processDetail］=> [{}]", page.getUrl());

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
            driver.get("https://www.google.com/");

            // 3.add Cookies 在工具类中解析json
            driver.manage().deleteAllCookies();

            WebDriverUtils.addCookies(driver, JsonToListUtil.amazonSourceCookieList2CookieList(JsonToListUtil.getListByPath(spiderConfig.getAmzVcCookieFilepath())));

            // 4.重定向跳转
            driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
            driver.get(page.getUrl().get());

            // 5.获取信息
            getDetail(page, driver);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
        } finally {
            driver.close();
        }

    }

    /**
     * 获取详情元素信息
     *
     * @param page
     * @param driver
     */
    private void getDetail(Page page, WebDriver driver) {

        // id
        String crawId = page.getRequest().getExtra("craw_id").toString();

        // 第一部分 Creation and approval summary
        WebElement casEle1 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//*[contains(text(),'Creation and approval summary')]/../../div[2]/div[2]"), 10);
        String createdOn = ObjectUtil.isNotEmpty(casEle1) ? casEle1.getText().trim() : "";
        WebElement casEle2 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//*[contains(text(),'Creation and approval summary')]/../../div[3]/div[2]"), 10);
        String promotionId = ObjectUtil.isNotEmpty(casEle2) ? casEle2.getText().trim() : "";
        WebElement casEle3 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//*[contains(text(),'Creation and approval summary')]/../../div[4]/div[2]"), 10);
        String status = ObjectUtil.isNotEmpty(casEle3) ? casEle3.getText().trim() : "";

        // 第二部分 promotion details
        WebElement pdEle1 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'Name')]/../td[2]"), 10);
        String name = ObjectUtil.isNotEmpty(pdEle1) ? pdEle1.getText().trim() : "";
        WebElement pdEle2 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'Start date')]/../td[2]"), 10);
        String startDate = ObjectUtil.isNotEmpty(pdEle2) ? pdEle2.getText().trim() : "";
        WebElement pdEle3 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'End date')]/../td[2]"), 10);
        String endDate = ObjectUtil.isNotEmpty(pdEle3) ? pdEle3.getText().trim() : "";
        WebElement pdEle4 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'Type')]/../td[2]"), 10);
        String type = ObjectUtil.isNotEmpty(pdEle4) ? pdEle4.getText().trim() : "";
//        WebElement pdEle5 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'Hero product')]/../td[2]"), 10);
//        String heroProduct = ObjectUtil.isNotEmpty(pdEle5) ? pdEle5.getText().trim() : "";
        // Dont have hero product on the page
        String heroProduct = "";
        WebElement pdEle6 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'Vendor code')]/../td[2]"), 10);
        String vendorCode = ObjectUtil.isNotEmpty(pdEle6) ? pdEle6.getText().trim() : "";
        WebElement pdEle7 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'Marketplace')]/../td[2]"), 10);
        String marketplace = ObjectUtil.isNotEmpty(pdEle7) ? pdEle7.getText().trim() : "";
        WebElement pdEle8 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'Billing contact')]/../td[2]"), 10);
        String billingContact = ObjectUtil.isNotEmpty(pdEle8) ? pdEle8.getText().trim() : "";
        WebElement pdEle9 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'Funding agreement')]/../td[2]"), 10);
        String fundingAgreement = ObjectUtil.isNotEmpty(pdEle9) ? pdEle9.getText().trim() : "";
        WebElement pdEle10 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'Merchandising fee')]/../td[2]"), 10);
        String merchandisingFee = ObjectUtil.isNotEmpty(pdEle10) ? pdEle10.getText().trim() : "";

        // 组装数据
        buildPromotionsInfo(page, crawId, createdOn, promotionId, status, name,
                startDate, endDate, type, heroProduct,
                vendorCode, marketplace, billingContact, fundingAgreement,
                merchandisingFee);

        // 第三部分 Included products
        List<WebElement> prudoctTrs = driver.findElements(By.xpath("//*[@id='promotion-products']//table[contains(@class,'a-horizontal-stripes')]//tr[@id!='head-row']"));
        for (WebElement productElement : prudoctTrs) {
            String asin = ObjectUtil.isNotEmpty(productElement.getAttribute("id")) ? productElement.getAttribute("id") : "";
            WebElement proEle1 = productElement.findElement(By.xpath("//*[@id='" + asin + "-name']"));
            String pName = ObjectUtil.isNotEmpty(proEle1) ? proEle1.getText().trim() : "";
            String upc = WebDriverUtils.isExistsElementFindByXpath(productElement, "//*[@id='promotion-select-product-id-vendor-id-label-" + asin + "']") ? productElement.findElement(By.xpath("//*[@id='promotion-select-product-id-vendor-id-label-" + asin + "']")).getText().trim() : "";
            WebElement proEle3 = productElement.findElement(By.xpath("//*[@id='" + asin + "-amazon-price']/span"));
            String amazonPrice = ObjectUtil.isNotEmpty(proEle3) ? proEle3.getText().trim() : "";
            WebElement proEle4 = productElement.findElement(By.xpath("//*[@id='promotion-select-product-id-website-price-']/span"));
            String promotionSelectProductIdWebsitePrice = ObjectUtil.isNotEmpty(proEle4) ? proEle4.getText().trim() : "";
            WebElement proEle5 = productElement.findElement(By.xpath("//*[@id='" + asin + "-funding']/span"));
            String funding = ObjectUtil.isNotEmpty(proEle5) ? proEle5.getText().trim() : "";
            WebElement proEle6 = productElement.findElement(By.xpath("//*[@id='" + asin + "-likely-price']/span"));
            String likelyPrice = ObjectUtil.isNotEmpty(proEle6) ? proEle6.getText().trim() : "";
            WebElement proEle7 = productElement.findElement(By.xpath("//*[@id='" + asin + "-metrics-units-sold-value']"));
            String unitsSold = ObjectUtil.isNotEmpty(proEle7) ? proEle7.getText().trim() : "";
            WebElement proEle8 = productElement.findElement(By.xpath("//*[@id='" + asin + "-metrics-amount-spent-value']"));
            String amountSpent = ObjectUtil.isNotEmpty(proEle8) ? proEle8.getText().trim() : "";
            WebElement proEle9 = productElement.findElement(By.xpath("//*[@id='" + asin + "-metrics-revenue-value']"));
            String revenue = ObjectUtil.isNotEmpty(proEle9) ? proEle9.getText().trim() : "";

            upc = upc.replace("UPC:", "").trim();
            amazonPrice = amazonPrice.replace("$", "");
            promotionSelectProductIdWebsitePrice = promotionSelectProductIdWebsitePrice.replace("$", "");
            funding = funding.replace("$", "");
            likelyPrice = likelyPrice.replace("$", "");
            amountSpent = amountSpent.replace("$", "");
            revenue = revenue.replace("$", "");

            buildPromotionsInfo(page, promotionId, crawId, asin, pName, upc, amazonPrice,
                    promotionSelectProductIdWebsitePrice, funding, likelyPrice, unitsSold,
                    amountSpent, revenue);
        }

    }

    /**
     * Add Promotion Info to page parameter
     * @param page
     * @param crawId
     * @param createdOnStr
     * @param promotionId
     * @param status
     * @param name
     * @param startDateStr
     * @param endDateStr
     * @param type
     * @param heroProduct
     * @param vendorCode
     * @param marketplace
     * @param billingContact
     * @param fundingAgreement
     * @param merchandisingFee
     */
    private void buildPromotionsInfo(Page page, String crawId, String createdOnStr, String promotionId, String status, String name,
                                     String startDateStr, String endDateStr, String type, String heroProduct,
                                     String vendorCode, String marketplace, String billingContact, String fundingAgreement,
                                     String merchandisingFee) {
        Object listObj = page.getResultItems().get("vcPromotionInfoDOList");
        List<VcPromotionInfoDO> vcPromotionInfoDOList;
        if (ObjectUtil.isNotEmpty(listObj)) {
            vcPromotionInfoDOList = (List<VcPromotionInfoDO>) listObj;
        } else {
            vcPromotionInfoDOList = new ArrayList<>();
        }
        vcPromotionInfoDOList.add(VcPromotionInfoDO.builder().billingContact(billingContact)
                .crawFlg("I")
                .crawId(crawId)
                .createdOnStr(createdOnStr)
                .createdOn(ConvertUtils.convertStringToLocalDateTime(createdOnStr, ConvertUtils.VC_PROMOTION_DATETIME))
                .endDateStr(endDateStr)
                .endDate(ConvertUtils.convertStringToLocalDateTime(endDateStr, ConvertUtils.VC_PROMOTION_DATETIME))
                .fundingAgreement(fundingAgreement)
                .heroProduct(heroProduct)
                .marketPlace(marketplace)
                .merchandisingFee(merchandisingFee)
                .name(name)
                .promotionId(promotionId)
                .startDateStr(startDateStr)
                .startDate(ConvertUtils.convertStringToLocalDateTime(startDateStr, ConvertUtils.VC_PROMOTION_DATETIME))
                .status(status)
                .type(type)
                .vendorCode(vendorCode).build());
        page.putField("vcPromotionInfoDOList", vcPromotionInfoDOList);


    }

    /**
     * Add Promotion Product Info to page parameters
     * @param page
     * @param promotionId
     * @param crawId
     * @param asin
     * @param pName
     * @param upc
     * @param amazonPriceStr
     * @param promotionSelectProductIdWebsitePriceStr
     * @param fundingStr
     * @param likelyPriceStr
     * @param unitsSoldStr
     * @param amountSpentStr
     * @param revenueStr
     */
    private void buildPromotionsInfo(Page page, String promotionId, String crawId, String asin, String pName, String upc, String amazonPriceStr,
                                     String promotionSelectProductIdWebsitePriceStr, String fundingStr, String likelyPriceStr, String unitsSoldStr,
                                     String amountSpentStr, String revenueStr) {
        Object listObj = page.getResultItems().get("vcPromotionProductInfoDOList");
        List<VcPromotionProductInfoDO> vcPromotionProductInfoDOList;
        if (ObjectUtil.isNotEmpty(listObj)) {
            vcPromotionProductInfoDOList = (List<VcPromotionProductInfoDO>) listObj;
        } else {
            vcPromotionProductInfoDOList = new ArrayList<>();
        }

        BigDecimal funding = ConvertUtils.convertStringToBigDecimal(fundingStr);
        BigDecimal likelyPrice = ConvertUtils.convertStringToBigDecimal(likelyPriceStr);
        BigDecimal amazonPrice = ConvertUtils.convertStringToBigDecimal(amazonPriceStr);
        BigDecimal websitePrice = ConvertUtils.convertStringToBigDecimal(promotionSelectProductIdWebsitePriceStr);;
        BigDecimal amountSpent = ConvertUtils.convertStringToBigDecimal(amountSpentStr);
        BigDecimal revenue = ConvertUtils.convertStringToBigDecimal(revenueStr);

        Integer unitsSold = ConvertUtils.convertStringToInteger(unitsSoldStr);

        vcPromotionProductInfoDOList.add(VcPromotionProductInfoDO.builder()
                .amazonPriceStr(amazonPriceStr)
                .amazonPrice(amazonPrice)
                .amountSpentStr(amountSpentStr)
                .amountSpent(amountSpent)
                .crawFlg("I")
                .crawId(crawId)
                .fundingStr(fundingStr)
                .funding(funding)
                .likelyPriceStr(likelyPriceStr)
                .likelyPrice(likelyPrice)
                .productName(pName)
                .promotionId(promotionId)
                .revenueStr(revenueStr)
                .revenue(revenue)
                .unitsSoldStr(unitsSoldStr)
                .unitsSold(unitsSold)
                .upc(upc)
                .websitePriceStr(promotionSelectProductIdWebsitePriceStr)
                .websitePrice(websitePrice)
                .asin(asin).build());

        page.putField("vcPromotionProductInfoDOList", vcPromotionProductInfoDOList);

    }


    public static void main(String[] args) {
        System.out.println("0.step67=>抓取程序开启。");

//        Spider.create(new AmazonVcPromotionsProcessor())
//                .addUrl("https://www.google.com/")
//                .run();
        Spider spider = Spider.create(new AmazonVcPromotionsProcessor(null));
        spider.addPipeline(new AmazonVcPromotionsPipeline());
        spider.addUrl("https://www.google.com/");
        spider.run();

        System.out.println("end.step93=>抓取程序结束。");

    }


}

