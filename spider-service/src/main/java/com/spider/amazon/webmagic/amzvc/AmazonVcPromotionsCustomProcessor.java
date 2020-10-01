package com.spider.amazon.webmagic.amzvc;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.exception.ServiceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.entity.Cookie;
import com.spider.amazon.mapper.VcPromotionInfoDOMapper;
import com.spider.amazon.model.Consts;
import com.spider.amazon.model.VcPromotionProcessorConfig;
import com.spider.amazon.model.VcPromotionInfoDO;
import com.spider.amazon.model.VcPromotionProductInfoDO;
import com.spider.amazon.remote.api.SpiderUrl;
import com.spider.amazon.service.CommonSettingService;
import com.spider.amazon.utils.ConvertUtils;
import com.spider.amazon.utils.CookiesUtils;
import com.spider.amazon.utils.WebDriverUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

/**
 * Amazon 买家中心Promotions抓取
 * TODO Promotion详情目前暂未支持单个Promotion10个以上产品的完整抓取
 * TODO 该版本是正常自动化跳转流程爬取
 */
@Component
@Slf4j
public class AmazonVcPromotionsCustomProcessor implements PageProcessor {

    private SpiderConfig spiderConfig;

    private CommonSettingService commonSettingService;

    private VcPromotionProcessorConfig vcPromotionProcessorConfig;

    private VcPromotionInfoDOMapper vcPromotionInfoDOMapper;

    private String asinPromotionIdXpath = "//*[@id=\"promotion-list-search\"]";
    private String asinSearchButtonXpath = "//*[@id=\"promotion-list-search-button-inner\"]";
    private String overlayXPath = "//div[@class='mt-loading-overlay']";

    @Autowired
    public AmazonVcPromotionsCustomProcessor(SpiderConfig spiderConfig, CommonSettingService commonSettingService, VcPromotionInfoDOMapper vcPromotionInfoDOMapper) {
        this.spiderConfig = spiderConfig;
        this.commonSettingService = commonSettingService;
        this.vcPromotionInfoDOMapper = vcPromotionInfoDOMapper;
    }

    public void setVcPromotionProcessorConfig(VcPromotionProcessorConfig vcPromotionProcessorConfig) {
        this.vcPromotionProcessorConfig = vcPromotionProcessorConfig;
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
    @Override
    public Site getSite() {
        return site;
    }

    /**
     * 爬取主逻辑
     *
     * @param page page
     */
    @Override
    public void process(Page page) {

        log.info("Extras:[{}]", page.getRequest().getExtras());

        log.info("抓取主逻辑");
        try{
            if (page.getUrl().regex(SpiderUrl.SPIDER_INDEX).match()) {
                // 列表页
                processMain(page);
            } else { // 详情页
                processDetail(page);
            }
        }catch (Exception e){
            e.printStackTrace();
            log.debug(e.getLocalizedMessage());
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
        System.setProperty("webdriver.chrome.driver", spiderConfig.getChromeDriverPath());
        WebDriver driver = WebDriverUtils.getWebDriver(spiderConfig.getDownloadPath(), false);

        ObjectMapper objectMapper = new ObjectMapper();

        try {

            // 1.0隐式等待对象声明
            WebDriverWait wait = new WebDriverWait(driver, 10);

            // 1.1设置页面超时等待时间,20S
            driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

            // 2.初始打开页面
            driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
            driver.get(SpiderUrl.AMAZON_VC_404);

            // 3.add Cookies from database
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

            // 4.重定向跳转
            driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
            driver.navigate().to(SpiderUrl.AMAZON_VC_PROMOTION);


            // 4.1 点击页记录数按钮
            WebElement recNumEle = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='a-autoid-1-announce']"), 10);
            WebDriverUtils.elementClick(recNumEle);
            sleep(3000);

            // 4.2 选择最大记录数
            WebElement maxNumEle = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-list-record-per-page-drop-down_2']"), 10);
            WebDriverUtils.elementClick(maxNumEle);

            WebDriverUtils.waitForLoad(driver);

            sleep(5000);

            // 4.3 Check processor config

            if(vcPromotionProcessorConfig == null){
                throw new Exception("You must set processor config");
            }

            // setting asins
            if(vcPromotionProcessorConfig != null && vcPromotionProcessorConfig.getAsins() != null){
                WebElement asinTextAreaEle = WebDriverUtils.expWaitForElement(driver, By.xpath(asinPromotionIdXpath), 10);

                Set<String> asinSet = vcPromotionProcessorConfig.getAsins().stream().filter(a -> StringUtils.isNotEmpty(a)).collect(Collectors.toSet());
                String searchStr = String.join(",", asinSet);

                log.info("Asin and Promotion Id text area: {}", searchStr);

                asinTextAreaEle.sendKeys(searchStr);

                WebElement asinSearchBtnEle = WebDriverUtils.expWaitForElement(driver, By.xpath(asinSearchButtonXpath), 10);

                WebDriverUtils.elementClick(asinSearchBtnEle);

                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(overlayXPath)));
            }

            // set end date before
            if (vcPromotionProcessorConfig != null && vcPromotionProcessorConfig.getEndDateBefore() != null){
                WebElement endDateToEle = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id=\"endDateBefore\"]"), 10);


                vcPromotionProcessorConfig.getEndDateBefore().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));

                String todayStr = vcPromotionProcessorConfig.getEndDateBefore().toString();

                // promotion end date before
                endDateToEle.sendKeys(todayStr);
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(overlayXPath)));
            }

            int pageIndex = 0;
            do {
                pageIndex++;

                log.info("page [{}]", pageIndex);
                WebDriverUtils.waitForLoad(driver);
                if (pageIndex != 1) {

                    String nextEleXPath = "//*[@id='promotion-list-pagination']/ul/li[contains(@class,'a-last')]/a";

                    WebElement nextEle = WebDriverUtils.expWaitForElement(driver, By.xpath(nextEleXPath), 20);
//                    ExpectedConditions.elementToBeClickable(nextEle);
//                    nextEle.click();
                    if (nextEle != null && nextEle.isEnabled()) {
                        WebDriverUtils.elementClick(nextEle);
                        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(overlayXPath)));
                    } else {
                        break;
                    }
                }

                // 5.获取页面链接
                WebElement tableElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='promotion-list']//table"), 20);
                List<WebElement> links = tableElement.findElements(By.xpath("//tr[@id!='head-row']//a"));
                for (int linksIndex = 0; linksIndex < links.size(); linksIndex++) {

                    String promotionDetailLink = links.get(linksIndex).getAttribute("href");

                    log.info("link=> [{}]", promotionDetailLink);

                    String promotionId = promotionDetailLink.split("/")[promotionDetailLink.split("/").length-1];

                    if(vcPromotionProcessorConfig.isSkipExist()){
                        if(StringUtils.isNotEmpty(promotionId) && !vcPromotionInfoDOMapper.existByPromotionId(promotionId)){
                            log.info("Promotion Id {} add page request", promotionId);
                            Request request = new Request(promotionDetailLink);
                            request.putExtra("craw_id", crawId);
                            page.addTargetRequest(request);
                        }else{
                            log.info("Promotion Id {} exist or not valid", promotionId);
                        }
                    }else{
                        Request request = new Request(promotionDetailLink);
                        request.putExtra("craw_id", crawId);
                        page.addTargetRequest(request);
                    }

                }
            } while (WebDriverUtils.isExistsElementFindByXpath(driver, By.xpath("//*[@id='promotion-list-pagination']/ul/li[contains(@class,'a-last')]/a"), 20));

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
        } finally {
            driver.quit();
        }

        if (log.isInfoEnabled()) {

            log.info("Total {} promotions", page.getTargetRequests().size());
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
        System.setProperty("webdriver.chrome.driver", spiderConfig.getChromeDriverPath());
        WebDriver driver = WebDriverUtils.getWebDriver(spiderConfig.getChromeDriverPath(), spiderConfig.getDownloadPath(), true);

        try {

            // 1.0隐式等待对象声明
            WebDriverWait wait = new WebDriverWait(driver, 10);

            // 1.1设置页面超时等待时间,20S
            driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

            // 2.初始打开页面
            driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
            driver.get(SpiderUrl.AMAZON_VC_404);

            // 3.add Cookies 在工具类中解析json
            driver.manage().deleteAllCookies();

            List<Cookie> cookies = commonSettingService.getAmazonVCCookies();

            List<org.openqa.selenium.Cookie> savedCookies = CookiesUtils.cookiesToSeleniumCookies(cookies);

            WebDriverUtils.addSeleniumCookies(driver, savedCookies);

            // 4.重定向跳转
            driver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS); // 页面加载超时时间
            driver.get(page.getUrl().get());

            // 5.获取信息
            getDetail(page, driver);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
        } finally {
            driver.quit();
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

}

