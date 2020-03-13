package com.spider.amazon.webmagic.amzvc;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.cons.DriverPathCons;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.entity.Cookie;
import com.spider.amazon.model.VcPromotionInfoDO;
import com.spider.amazon.model.VcPromotionProductInfoDO;
import com.spider.amazon.utils.CookiesUtils;
import com.spider.amazon.utils.JsonToListUtil;
import com.spider.amazon.utils.WebDriverUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Amazon 买家中心Promotions抓取
 * TODO Promotion详情目前暂未支持单个Promotion10个以上产品的完整抓取
 * TODO 该版本是正常自动化跳转流程爬取
 */
@Component
@Slf4j
public class AmazonVcPromotionsProcessor implements PageProcessor {

    public static final String INDEX_URL="https://www.google.com/";
    public static final String MAIN_URL="https://vendorcentral.amazon.com/hz/vendor/members/promotions/list/home?ref_=vc_xx_subNav";

    @Autowired
    private CookiesUtils cookiesUtils;

    @Value("${amazon.vc.freelogin.cookies.name}")
    private String cookiesConfigName;

    private Site site = Site
            .me()
            .setRetryTimes(3)
            .setDomain("https://vendorcentral.amazon.com/hz/vendor/members/promotions/list/home?ref_=vc_xx_subNav")
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
     * 爬取主逻辑
     * @param page page
     */
    public void process(Page page) {

        log.info("Extras:[{}]",page.getRequest().getExtras());

        log.info("抓取主逻辑");
        if (page.getUrl().regex(INDEX_URL).match()) { // 列表页
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
        String crawId=DateUtil.format(DateUtil.date(), DateFormat.YEAR_MONTH_DAY_yyyyMMddHHmmss1);

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
            List<Cookie> listCookies = JsonToListUtil.amazonSourceCookieList2CookieList(JsonToListUtil.getList());
            for (Cookie cookie : listCookies) {
                // Cookie(String name, String value, String domain, String path, Date expiry, boolean isSecure, boolean isHttpOnly)
                driver.manage().addCookie(new org.openqa.selenium.Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(),
                        cookie.getPath(), cookie.getExpiry(), cookie.getIsSecure(), cookie.getIsHttpOnly()));
            }

            // 4.重定向跳转
            driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
            driver.get("https://vendorcentral.amazon.com/hz/vendor/members/promotions/list/home?ref_=vc_xx_subNav");


            // 4.1 点击页记录数按钮
            WebElement recNumEle=WebDriverUtils.expWaitForElement(driver,By.xpath("//*[@id='a-autoid-1-announce']"),10);
            recNumEle.click();

            // 4.2 选择最大记录数
            WebElement maxNumEle=WebDriverUtils.expWaitForElement(driver,By.xpath("//*[@id='promotion-list-record-per-page-drop-down_2']"),10);
            maxNumEle.click();

            int pageIndex=0;
            do {
                pageIndex++;
                log.info("page [{}]",pageIndex);
                if (pageIndex!=1) {
                    WebElement nextEle=WebDriverUtils.expWaitForElement(driver,By.xpath("//*[@id='promotion-list-pagination']/ul/li[contains(@class,'a-last')]/a"),10);
                    nextEle.click();
                }

                // 5.获取页面链接
                WebElement tableElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-list']//table"), 20);
                List<WebElement> links = tableElement.findElements(By.xpath("//tr[@id!='head-row']//a"));
                for (int linksIndex=0;linksIndex<links.size();linksIndex++) {
                    log.info("link=> [{}]",links.get(linksIndex).getAttribute("href"));
                    Request request=new Request(links.get(linksIndex).getAttribute("href"));
                    request.putExtra("craw_id",crawId);
                    page.addTargetRequest(request);
                }
            } while (WebDriverUtils.isExistsElementFindByXpath(driver,By.xpath("//*[@id='promotion-list-pagination']/ul/li[contains(@class,'a-last')]/a"),20));

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(),RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
        } finally {
            driver.close();
        }

        if (log.isInfoEnabled()) {
            log.info("1.step84=>抓取结束");
        }

    }

    /**
     * 详情页面爬取
     * @param page
     */
    private void processDetail(Page page) {
        log.info("［processDetail］=> [{}]",page.getUrl());

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
            List<Cookie> listCookies = JsonToListUtil.amazonSourceCookieList2CookieList(JsonToListUtil.getList());
            for (Cookie cookie : listCookies) {
                // Cookie(String name, String value, String domain, String path, Date expiry, boolean isSecure, boolean isHttpOnly)
                driver.manage().addCookie(new org.openqa.selenium.Cookie(cookie.getName(), cookie.getValue(), cookie.getDomain(),
                        cookie.getPath(), cookie.getExpiry(), cookie.getIsSecure(), cookie.getIsHttpOnly()));
            }

            // 4.重定向跳转
            driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
            driver.get(page.getUrl().get());

            // 5.获取信息
            getDetail(page,driver);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(),RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
        } finally {
            driver.close();
        }

    }

    /**
     * 获取详情元素信息
     * @param page
     * @param driver
     */
    private void getDetail(Page page,WebDriver driver) {

        // id
        String crawId=page.getRequest().getExtra("craw_id").toString();

        // 第一部分 Creation and approval summary
        WebElement casEle1 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//*[contains(text(),'Creation and approval summary')]/../../div[2]/div[2]"), 10);
        String createdOn= ObjectUtil.isNotEmpty(casEle1)? casEle1.getText().trim():"";
        WebElement casEle2 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//*[contains(text(),'Creation and approval summary')]/../../div[3]/div[2]"), 10);
        String promotionId= ObjectUtil.isNotEmpty(casEle2)? casEle2.getText().trim():"";
        WebElement casEle3 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//*[contains(text(),'Creation and approval summary')]/../../div[4]/div[2]"), 10);
        String status= ObjectUtil.isNotEmpty(casEle3)? casEle3.getText().trim():"";

        // 第二部分 promotion details
        WebElement pdEle1 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'Name')]/../td[2]"), 10);
        String name= ObjectUtil.isNotEmpty(pdEle1)? pdEle1.getText().trim():"";
        WebElement pdEle2 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'Start date')]/../td[2]"), 10);
        String startDate= ObjectUtil.isNotEmpty(pdEle2)? pdEle2.getText().trim():"";
        WebElement pdEle3 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'End date')]/../td[2]"), 10);
        String endDate= ObjectUtil.isNotEmpty(pdEle3)? pdEle3.getText().trim():"";
        WebElement pdEle4 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'Type')]/../td[2]"), 10);
        String type= ObjectUtil.isNotEmpty(pdEle4)? pdEle4.getText().trim():"";
        WebElement pdEle5 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'Hero product')]/../td[2]"), 10);
        String heroProduct = ObjectUtil.isNotEmpty(pdEle5)? pdEle5.getText().trim():"";
        WebElement pdEle6 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'Vendor code')]/../td[2]"), 10);
        String vendorCode = ObjectUtil.isNotEmpty(pdEle6)? pdEle6.getText().trim():"";
        WebElement pdEle7 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'Marketplace')]/../td[2]"), 10);
        String marketplace = ObjectUtil.isNotEmpty(pdEle7)? pdEle7.getText().trim():"";
        WebElement pdEle8 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'Billing contact')]/../td[2]"), 10);
        String billingContact = ObjectUtil.isNotEmpty(pdEle8)? pdEle8.getText().trim():"";
        WebElement pdEle9 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'Funding agreement')]/../td[2]"), 10);
        String fundingAgreement = ObjectUtil.isNotEmpty(pdEle9)? pdEle9.getText().trim():"";
        WebElement pdEle10 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-detail-page']//table[contains(@class,'a-spacing-top-micro')]//td[contains(text(),'Merchandising fee')]/../td[2]"), 10);
        String merchandisingFee = ObjectUtil.isNotEmpty(pdEle10)? pdEle10.getText().trim():"";

        // 组装数据
        buildPromotionsInfo( page, crawId ,  createdOn,  promotionId,  status,  name,
                 startDate,  endDate,  type,  heroProduct,
                 vendorCode,  marketplace,  billingContact,  fundingAgreement,
                 merchandisingFee);

        // 第三部分 Included products
        List<WebElement> prudoctTrs = driver.findElements(By.xpath("//*[@id='promotion-products']//table[contains(@class,'a-horizontal-stripes')]//tr[@id!='head-row']"));
        for (WebElement productElement:prudoctTrs) {
            String asin =  ObjectUtil.isNotEmpty(productElement.getAttribute("id"))? productElement.getAttribute("id"):"";
            WebElement proEle1 = productElement.findElement(By.xpath("//*[@id='"+asin+"-name']"));
            String pName = ObjectUtil.isNotEmpty(proEle1)? proEle1.getText().trim():"";
            String upc=WebDriverUtils.isExistsElementFindByXpath(productElement,"//*[@id='promotion-select-product-id-vendor-id-label-"+asin+"']")? productElement.findElement(By.xpath("//*[@id='promotion-select-product-id-vendor-id-label-"+asin+"']")).getText().trim():"";
            WebElement proEle3 = productElement.findElement(By.xpath("//*[@id='"+asin+"-amazon-price']/span"));
            String amazonPrice = ObjectUtil.isNotEmpty(proEle3)? proEle3.getText().trim():"";
            WebElement proEle4 = productElement.findElement(By.xpath("//*[@id='promotion-select-product-id-website-price-']/span"));
            String promotionSelectProductIdWebsitePrice = ObjectUtil.isNotEmpty(proEle4)? proEle4.getText().trim():"";
            WebElement proEle5 = productElement.findElement(By.xpath("//*[@id='"+asin+"-funding']/span"));
            String funding = ObjectUtil.isNotEmpty(proEle5)? proEle5.getText().trim():"";
            WebElement proEle6 = productElement.findElement(By.xpath("//*[@id='"+asin+"-likely-price']/span"));
            String likelyPrice = ObjectUtil.isNotEmpty(proEle6)? proEle6.getText().trim():"";
            WebElement proEle7 = productElement.findElement(By.xpath("//*[@id='"+asin+"-metrics-units-sold-value']"));
            String unitsSold = ObjectUtil.isNotEmpty(proEle7)? proEle7.getText().trim():"";
            WebElement proEle8 = productElement.findElement(By.xpath("//*[@id='"+asin+"-metrics-amount-spent-value']"));
            String amountSpent = ObjectUtil.isNotEmpty(proEle8)? proEle8.getText().trim():"";
            WebElement proEle9 = productElement.findElement(By.xpath("//*[@id='"+asin+"-metrics-revenue-value']"));
            String revenue = ObjectUtil.isNotEmpty(proEle9)? proEle9.getText().trim():"";

            buildPromotionsInfo( page, promotionId , crawId, asin, pName, upc, amazonPrice,
                     promotionSelectProductIdWebsitePrice, funding, likelyPrice, unitsSold,
                     amountSpent, revenue);
        }

    }

    private void buildPromotionsInfo(Page page,String crawId , String createdOn, String promotionId, String status, String name,
                                                        String startDate, String endDate, String type, String heroProduct,
                                                        String vendorCode, String marketplace, String billingContact, String fundingAgreement,
                                                        String merchandisingFee) {
        Object listObj = page.getResultItems().get("vcPromotionInfoDOList");
        List<VcPromotionInfoDO> vcPromotionInfoDOList;
        if(ObjectUtil.isNotEmpty(listObj)) {
            vcPromotionInfoDOList = (List<VcPromotionInfoDO>) listObj;
        } else {
            vcPromotionInfoDOList = new ArrayList<>();
        }
        vcPromotionInfoDOList.add(VcPromotionInfoDO.builder().billingContact(billingContact)
                .crawFlg("I")
                .crawId(crawId)
                .createdOn(createdOn)
                .endDate(endDate)
                .fundingAgreement(fundingAgreement)
                .heroProduct(heroProduct)
                .marketPlace(marketplace)
                .merchandisingFee(merchandisingFee)
                .name(name)
                .promotionId(promotionId)
                .startDate(startDate)
                .status(status)
                .type(type)
                .vendorCode(vendorCode).build());
        page.putField("vcPromotionInfoDOList",vcPromotionInfoDOList);


    }

    private void buildPromotionsInfo(Page page,String promotionId ,String crawId,String asin,String pName,String upc,String amazonPrice,
                                     String promotionSelectProductIdWebsitePrice,String funding,String likelyPrice,String unitsSold,
                                     String amountSpent,String revenue) {
        Object listObj = page.getResultItems().get("vcPromotionProductInfoDOList");
        List<VcPromotionProductInfoDO> vcPromotionProductInfoDOList;
        if(ObjectUtil.isNotEmpty(listObj)) {
            vcPromotionProductInfoDOList = (List<VcPromotionProductInfoDO>) listObj;
        } else {
            vcPromotionProductInfoDOList = new ArrayList<>();
        }

        vcPromotionProductInfoDOList.add(VcPromotionProductInfoDO.builder()
                .amazonPrice(amazonPrice)
                .amountSpent(amountSpent)
                .crawFlg("I")
                .crawId(crawId)
                .funding(funding)
                .likelyPrice(likelyPrice)
                .pname(pName)
                .promotionId(promotionId)
                .revenue(revenue)
                .unitsSold(unitsSold)
                .upc(upc)
                .websitePrice(promotionSelectProductIdWebsitePrice)
                .asin(asin).build());

        page.putField("vcPromotionProductInfoDOList",vcPromotionProductInfoDOList);

    }



    public static void main(String[] args) {
        System.out.println("0.step67=>抓取程序开启。");

        Spider.create(new AmazonVcPromotionsProcessor())
                .addUrl("https://www.google.com/")
                .run();

        System.out.println("end.step93=>抓取程序结束。");

    }


}

