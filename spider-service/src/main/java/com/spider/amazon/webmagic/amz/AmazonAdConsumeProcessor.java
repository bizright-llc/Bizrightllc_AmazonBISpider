package com.spider.amazon.webmagic.amz;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.cons.*;
import com.spider.amazon.dto.AmazonAdConsumeItemDTO;
import com.spider.amazon.dto.AmazonAdConsumeSettingDTO;
import com.spider.amazon.dto.AmazonAdDTO;
import com.spider.amazon.remote.api.SpiderUrl;
import com.spider.amazon.service.AmazonAdService;
import com.spider.amazon.utils.WebDriverUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.spider.amazon.cons.AmazonAdNodeType.*;
import static java.lang.Thread.sleep;

/**
 * Amazon广告消耗功能
 * TODO DEMO暂未进行功能解耦
 */
@Component
@Slf4j
public class AmazonAdConsumeProcessor implements PageProcessor {

    // 关键词列表,在输入框循环输入进行搜索的关键词,example:xxx|xxx|xxx|
    public final static String SEARCH_LIST = "SEARCH_LIST";
    // 广告黑名单,在黑名单上会进行广告消耗逻辑
    public final static String BLACK_LIST = "BLACK_LIST";
    // 广告白名单，在白名单上的关键词商品不会进行消耗逻辑
    public final static String WHITE_LIST = "WHITE_LIST";
    public final static String LOCATION = "10001";

    // Xpath List
    // location xpath
    public final static String LOCATION_XPATH = "//*[@id='glow-ingress-line2' and contains(text(),'10001')]"; // Location 位置以LA位置为准
    public final static String LOCATION_CLICK = "//DIV[@id='nav-packard-glow-loc-icon']"; // 定位点击
    public final static String LOCATION_CHANGE_BUTTON = "//*[@id='GLUXChangePostalCodeLink']"; // 定位改变按钮
    public final static String LOCATION_INPUT_TEXT = "//*[@id='GLUXZipUpdateInput']"; // 定位信息输入框
    public final static String LOCATION_INPUT_CHECK = "//*[@id='GLUXZipUpdate-announce']/../input"; // 定位信息确认
    public final static String LOCATION_INPUT_DOWN = "//*[@name='glowDoneButton']"; // 定位信息DOWN
    public final static String LOCATION_INPUT_CONTINUE = "//*[@id='GLUXConfirmClose']"; // 定位信息刷新按钮
    // Search Result Sponsored Ad
    public final static String SEARCH_INPUT_ELE = "//*[@id='twotabsearchtextbox']"; // 搜索框输入框
    public final static String SEARCH_CLICK_ELE = "//*[@id='nav-search-submit-text']"; // 搜索框搜索按钮
    public final static String INDEX_SPONSORED_XPATH = "//*[@id='search']//div[contains(@class,'s-search-results')]/div[@data-index!='']"; // 搜索页广告元素xpath
    public final static String SEARCH_RESULT_SPONSORED_ITEMS_XPATH = "//div[*//div[contains(@data-component-type,'sp-sponsored-result')] and @data-asin != '']";
    public final static String SEARCH_RESULT_ITEM_TITLE_XPATH = ".//span[contains(@class, 'a-size-medium') and contains(@class, 'a-text-normal')]";
    public final static String SEARCH_RESULT_ITEM_TITLE_XPATH2 = ".//span[contains(@class, 'a-size-base-plus') and contains(@class, 'a-text-normal')]";
    public final static String INDEX_SPONSORED_REXPATH = "//*[@id='search']//div[contains(@class,'s-search-results')]/div[@data-index='{dataIndex}']"; // 搜索页广告元素xpath
    public final static String INDEX_CHILD_TEXT_XPATH = ".//span[@cel_widget_id='SEARCH_RESULTS-SEARCH_RESULTS']//img"; // 广告元素子节点TEXT，用于筛选是否是黑白名单
    public final static String INDEX_ISSPONSORED_XPATH = ".//span[text()='Sponsored']"; // 广告元素子节点TEXT，用于筛选是否是广告商品
    public final static String REDIRECT_DETAIL_XPATH = ".//span[@cel_widget_id='SEARCH_RESULTS-SEARCH_RESULTS']//img/../../../a[1]"; // 广告元素跳转详情页面链接地址元素
    //  The sponsored ad of the search result page, not included the result items
    public final static String SEARCH_RESULT_INNER_AD_XPATH = "//div[contains(@class, '_multi-card-creative-desktop_Container_container')]";
    public final static String SEARCH_RESULT_INNER_AD_XPATH2 = "//div[contains(@data-creative-type, 'MultiCardCreativeDesktop')]";
    public final static String SEARCH_RESULT_INNER_AD_INFO_XPATH = ".//a[contains(@aria-label, 'Sponsored ad from')]";
    public final static String SEARCH_RESULT_INNER_AD_ITEMS_XPATH = ".//div[@data-asin]";
    public final static String SEARCH_RESULT_BRAND_RELATE_AD_XPATH = "//div[contains(@data-creative-type, 'MultiBrandCreativeDesktop')]";
    public final static String SEARCH_RESULT_BRAND_RELATE_AD_INFO_XPATH = ".//a[contains(@aria-label, 'Sponsored ad from')]";
    public final static String SEARCH_RESULT_ITEMS_IMAGE_XPATH = ".//img[contains(@class, 's-image')]";
    public final static String SEARCH_RESULT_ITEMS_NAME_XPATH = ".//h2[contains(@class, 'a-size-mini')]";
    public final static String SEARCH_RESULT_NEXT_PAGE_XPATH = "//span[contains(@class, 'PAGINATION')]//li[contains(@class, 'last')]";
    // 详情页广告商品出现位置xpath
    public final static String DETAIL_IFRAME_XPATH = "//*[@id='ape_Detail_hero-quick-promo_Desktop_iframe']"; // 详情下方iframe
    public final static String DETAIL_IFRAME_REDIRECT_XPATH = ".//*[@id='sp_hqp_shared_inner']/div/a"; // 详情下方iframe重定向地址
    public final static String DETAIL_IFRAME_TEXT_XPATH = ".//*[@id='sp_hqp_shared_inner']/div/a"; // 详情下方iframe过滤文案
    public final static String DETAIL_IFRAME_TEXT_XPATH2 = ".//a[@id='title']"; // 详情下方iframe过滤文案
    // 购物车下方广告商品出现位置xpath
    public final static String BUYBOX_IFRAME_XPATH = "//*[@id='ape_Detail_ams-detail-right-v2_desktop_iframe']"; // 购物车下方iframe
    public final static String BUYBOX_IFRAME_REDIRECT_XPATH = ".//*[@id='ape_Detail_ams-detail-right-v2_desktop_iframe']"; // 购物车下方iframe重定向地址
    public final static String BUYBOX_IFRAME_TEXT_XPATH = ".//*[@id='ape_Detail_ams-detail-right-v2_desktop_iframe']"; // 购物车下方iframe过滤文案
    // Brand relate ad
    public final static String DETAIL_BRAND_RELATE_AD_XPATH = "//div[contains(@class, 'sbx_mbd')]";
    public final static String DETAIL_BRAND_RELATE_AD_TITLE_XPATH="*//div[contains(@data-click-el, 'headline')]";
    public final static String DETAIL_BRAND_RELATE_AD_SHOP_LINK_XPATH="*//div[contains(@data-click-el, 'cta')]";
    // 详情页新版本商品位置xpath
    public final static String DETAIL_VERSION_XPATH = "//*[@id='newer-version']"; // 产品系列新版本
    public final static String DETAIL_VERSION_REDIRECT_XPATH = ".//*[@id='newer-version']"; // 产品系列新版本
    public final static String DETAIL_VERSION_TEXT_XPATH = ".//*[@id='newer-version']"; // 产品系列新版本
    // 同类型广告产品列表位置xpath
    public final static String SAME_PRODUCT_XPATH = "//*[@id='sims-consolidated-2_feature_div']//ol"; // 同类型广告产品列表
    public final static String SAME_PRODUCT_REDIRECT_XPATH = ".//*[@id='sims-consolidated-2_feature_div']//ol"; // 同类型广告产品列表
    public final static String SAME_PRODUCT_TEXT_XPATH = ".//*[@id='sims-consolidated-2_feature_div']//ol"; // 同类型广告产品列表

    private SpiderConfig spiderConfig;

    private AmazonAdService amazonAdService;

    private List<AmazonAdConsumeSettingDTO> amazonAdSettings = null;

    private AmazonAdConsumeSettingDTO currentSetting = null;

    private Site site = Site
            .me()
            .setRetryTimes(3)
            .setDomain(SpiderUrl.AMAZON_INDEX)
            .setSleepTime(3000)
            .setUserAgent(
                    "User-Agent:Mozilla/5.0(Macintosh;IntelMacOSX10_7_0)AppleWebKit/535.11(KHTML,likeGecko)Chrome/17.0.963.56Safari/535.11");

    @Autowired
    public AmazonAdConsumeProcessor(SpiderConfig spiderConfig, AmazonAdService amazonAdService) {
        this.spiderConfig = spiderConfig;
        this.amazonAdService = amazonAdService;
    }

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

        amazonAdSettings = amazonAdService.getAllActiveSetting();

        Map<String, Object> params = new HashMap<>();

        // 1.建立WebDriver
        // use proxy setting driver
        WebDriver driver = WebDriverUtils.getWebDriverWithProxy(spiderConfig.getChromeDriverPath(), spiderConfig.getDownloadPath(), spiderConfig.getChromeProxyFilepath(), false);

        // TODO: Only for testing, change proxy driver
//        WebDriver driver = WebDriverUtils.getWebDriver(spiderConfig.getChromeDriverPath(), spiderConfig.getDownloadPath(), false);

        try {

            // 1.0设置页面超时等待时间,5S
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

            // 2.初始打开页面
            driver.get(SpiderUrl.AMAZON_INDEX);
            log.info("Headers=>[{}]", page.getRequest());

            // 3.1 定位输入框输入当前循环参数（外层循环）
            for(AmazonAdConsumeSettingDTO setting: amazonAdSettings){
                List<String> settingSearchWords = Arrays.stream(setting.getSearchWords().split(",")).filter(w -> StringUtils.isNotEmpty(w.trim())).collect(Collectors.toList());

                // shuffle search sequence
                Collections.shuffle(settingSearchWords);

                for (String searchWord: settingSearchWords) {
                    // if search bar exist
                    if (isExistsSearchBox(driver)) {
//                    driver.get(SpiderUrl.AMAZON_INDEX);
                        WebElement searchElement = WebDriverUtils.expWaitForElement(driver, By.xpath(SEARCH_INPUT_ELE), 10);
                        WebElement searchClickElement = WebDriverUtils.expWaitForElement(driver, By.xpath(SEARCH_CLICK_ELE), 10);
                        searchElement.clear();
                        searchElement.sendKeys(searchWord);
                        boolean searchClicked = WebDriverUtils.isClicked(driver, searchClickElement);
                        if(searchClicked == false){
                            log.debug("[process] search button click failed");
                            continue;
                        }

                        for (int i=0; i< 3; i++){

                            WebDriverUtils.randomSleepBetween(3000,5000);

                            // Find sponsored items
                            List<AmazonAdDTO> sponsoredProductList = locateSponsoredProduct(driver);

                            // shuffle the ads
                            Collections.shuffle(sponsoredProductList);

                            // 4.1黑白名单过滤广告商品，过滤后需攻击商品进入消耗列表
                            // TODO 该部分消耗列表使用schedule，或是直接点击进入待确定
                            // 循环点击进入商品详情页
                            for (AmazonAdDTO sponsoredProduct : sponsoredProductList) {

                                WebElement productElement = null;
                                if (!isSponsoredPro(sponsoredProduct, setting)) {
                                    continue;
                                }

                                sponsoredProduct.setSettingId(setting.getId());

                                // reget element
                                if (sponsoredProduct.getType() == SEARCH_RESULT_AD){
                                    productElement = WebDriverUtils.expWaitForElement(driver, By.xpath(INDEX_SPONSORED_REXPATH.replace("{dataIndex}", sponsoredProduct.getIndex())), 60);
                                }else{
                                    productElement = sponsoredProduct.getWebElement();
                                }

                                if(ObjectUtil.isNotNull(productElement)){
                                    // click item
                                    int result = processSponsoredAd(driver, productElement, sponsoredProduct, setting);
                                    WebDriverUtils.randomSleepBetween(3000, 5000);
                                    if (result != RespResult.SUCC_OOM) {
                                        log.debug("[process] Process ad {} failed", sponsoredProduct);
                                        continue;
                                    }
                                }else{
                                    log.debug("[process] sponsor product element not found");
                                }

                            }

                            WebElement nextPageEle = WebDriverUtils.expWaitForElement(driver, By.xpath(SEARCH_RESULT_NEXT_PAGE_XPATH), 30);

                            if(nextPageEle != null){
                                WebDriverUtils.isClicked(driver, nextPageEle);
                            }else {
                                break;
                            }
                        }

                    } else {
                        break;
                    }
                }
            }

            try {
                sleep(30000);
            } catch (InterruptedException e) {
                log.debug("[] InterruptedException", e);
                throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
            }
        } catch (Exception e) {
            log.debug("[process] process failed", e);
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
        } finally {
            driver.quit();
        }

        try{
            amazonAdService.persistLog();
        }catch (Exception ex){
            log.debug("[process] Persist ad consume log failed", ex);
        }

        if (log.isInfoEnabled()) {
            log.info("1.step84=>抓取结束");
        }

    }

    /**
     * Check the amazon ad need consume or not
     *
     * @param amazonAd
     * @return
     */
    private boolean isSponsoredPro(AmazonAdDTO amazonAd, AmazonAdConsumeSettingDTO setting) {

        boolean result = amazonAdService.consume(amazonAd, setting);

        log.debug("[isSponsoredPro] amazon ad {} consume {}", amazonAd, result);

        return result;

    }

    private void changeLocation(WebDriver driver, Page page) {

        log.info("Change Location");

        // 1.查找定位标签
        WebElement locationElement = WebDriverUtils.expWaitForElement(driver, By.xpath(LOCATION_XPATH), 10);
        if (ObjectUtil.isNotEmpty(locationElement)) { // 已经切换至目标区域,中断返回
            return;
        }

        // 2.点击定位切换
        WebElement locationClick = WebDriverUtils.expWaitForElement(driver, By.xpath(LOCATION_CLICK), 10);
        if (ObjectUtil.isNotEmpty(locationClick)) {
            WebDriverUtils.isClicked(driver, locationClick);
        }

        // 3.查找change按钮
        WebElement changeElement = WebDriverUtils.expWaitForElement(driver, By.xpath(LOCATION_CHANGE_BUTTON), 10);
        if (ObjectUtil.isNotEmpty(changeElement)) { // 包含change按钮，先进行点击change按钮操作
            WebDriverUtils.isClicked(driver, changeElement);
        }

        // 4.查找定位输入框输入定位代码
        WebElement inputElement = WebDriverUtils.expWaitForElement(driver, By.xpath(LOCATION_INPUT_TEXT), 10);
        if (ObjectUtil.isNotEmpty(inputElement)) {
            inputElement.sendKeys(LOCATION);
        }

        // 5.查找定位确定按钮点击
        WebElement inputCheckElement = WebDriverUtils.expWaitForElement(driver, By.xpath(LOCATION_INPUT_CHECK), 10);
        if (ObjectUtil.isNotEmpty(inputCheckElement)) {
            inputCheckElement.click();
        }

        // 6.查询down按钮
        WebElement inputDownElement = WebDriverUtils.expWaitForElement(driver, By.xpath(LOCATION_INPUT_DOWN), 5);
        if (ObjectUtil.isNotEmpty(inputDownElement)) {
            inputDownElement.click();
        }

        // 6.查看页面是否切换成功
        WebElement locationElementNow = WebDriverUtils.expWaitForElement(driver, By.xpath(LOCATION_XPATH), 5);
        if (ObjectUtil.isNotEmpty(locationElementNow)) { // 已经切换至目标区域,中断返回
            return;
        }

        // 7.刷新页面
        driver.navigate().refresh();

        return;
    }


    /**
     * 判断是否存在搜索框
     *
     * @param driver
     * @return
     */
    private boolean isExistsSearchBox(WebDriver driver) {
        log.info("[isExistsSearchBox] check search box");
        WebElement element = WebDriverUtils.expWaitForElement(driver, By.xpath(SEARCH_INPUT_ELE), 10);
        WebElement clickElement = WebDriverUtils.expWaitForElement(driver, By.xpath(SEARCH_CLICK_ELE), 10);
        // search box not exist
        if (ObjectUtil.isEmpty(element) || ObjectUtil.isEmpty(clickElement)) {
            return false;
        }
        return true;
    }

    /**
     * Find result sponsored items,
     * inner ad and items
     *
     * @param driver
     * @return
     */
    private List<AmazonAdDTO> locateSponsoredProduct(WebDriver driver) {
        // 返回过滤处理后的商品广告元素
        List<AmazonAdDTO> sponsoredProductList = new ArrayList<>();
        // Get search result items sponsored ad
        List<WebElement> productElementList = WebDriverUtils.expWaitForElements(driver, By.xpath(SEARCH_RESULT_SPONSORED_ITEMS_XPATH), 60);
        // 遍历产品元素，黑白名单筛选需要进一步点击的商品
        for (WebElement productElement : productElementList) {
            log.debug("asin=>[{}]", productElement.getAttribute("data-asin"));

            if (StringUtils.isNotEmpty(productElement.getAttribute("data-asin")) && StringUtils.isNotEmpty(productElement.getAttribute("data-index"))) {

                String adAsin = productElement.getAttribute("data-asin");
                String adIndex = productElement.getAttribute("data-index");

                WebElement titleEle = null;

                try{
                    List<WebElement> findEles = productElement.findElements(By.xpath(SEARCH_RESULT_ITEM_TITLE_XPATH));
                    if(findEles.size() > 0){
                        titleEle = findEles.get(0);
                    }

                    if(titleEle == null){
                        findEles = productElement.findElements(By.xpath(SEARCH_RESULT_ITEM_TITLE_XPATH2));

                        if(findEles.size() > 0){
                            titleEle = findEles.get(0);
                        }
                    }

                }catch (Exception ex){
                    log.info("[locateSponsoredProduct] {} get title failed", StringUtils.isNotEmpty(adAsin) ? adAsin : adIndex, ex);
                }

                sponsoredProductList.add(AmazonAdDTO.builder()
                        .type(SEARCH_RESULT_AD)
                        .asin(productElement.getAttribute("data-asin"))
                        .title(ObjectUtil.isNotEmpty(titleEle) ? titleEle.getText() : "")
                        .index(productElement.getAttribute("data-index"))
                        .build());
            }

        }

        // find inner ad
        List<WebElement> innerSponsoredEles = WebDriverUtils.expWaitForElements(driver, By.xpath(SEARCH_RESULT_INNER_AD_XPATH), 60);

        if(innerSponsoredEles == null || innerSponsoredEles.size() == 0){
            innerSponsoredEles = WebDriverUtils.expWaitForElements(driver, By.xpath(SEARCH_RESULT_INNER_AD_XPATH2), 60);
        }

        for (WebElement sponsoredEle: innerSponsoredEles){

            AmazonAdDTO innerAd = new AmazonAdDTO();

            innerAd.setType(SEARCH_RESULT_INNER_AD);
            innerAd.setWebElement(sponsoredEle);

            String brand = "";
            try{
                WebElement brandInfoEle = sponsoredEle.findElement(By.xpath(SEARCH_RESULT_INNER_AD_INFO_XPATH));

                String labelStr = brandInfoEle.getAttribute("aria-label");

                brand = labelStr.substring(labelStr.indexOf("from")+5, labelStr.indexOf("."));

                innerAd.setBrand(brand);
            }catch (Exception ex){
                log.debug("[locateSponsoredProduct] Parse inner ad brand failed", ex);
            }

//            sposoredProductList.add(innerAd);

            List<WebElement> sponsoredEleItems = new ArrayList<>();

            try{
//                sponsoredEleItems = sponsoredEle.findElements(By.xpath(SEARCH_RESULT_INNER_AD_ITEMS_XPATH));
                sponsoredEleItems = sponsoredEle.findElements(By.xpath(SEARCH_RESULT_INNER_AD_ITEMS_XPATH));

                for (WebElement itemEle: sponsoredEleItems){
                    AmazonAdDTO innerItemAd = new AmazonAdDTO();
                    innerItemAd.setType(SEARCH_RESULT_INNER_ITEM_AD);
                    innerItemAd.setWebElement(itemEle);
                    innerItemAd.setBrand(brand);

                    try{
                        String asin = itemEle.getAttribute("data-asin");
                        innerItemAd.setAsin(asin);
                    }catch (Exception ex){
                        log.debug("[locateSponsoredProduct] Get inner item asin failed");
                    }

                    sponsoredProductList.add(innerItemAd);
                }

            }catch (Exception ex){
                log.debug("[locateSponsoredProduct] Inner sponsored element no items");
            }
        }

        // parse brand relate ads
        List<WebElement> brandRelateAds = WebDriverUtils.expWaitForElements(driver, By.xpath(SEARCH_RESULT_BRAND_RELATE_AD_XPATH), 30);

        for (WebElement adEle: brandRelateAds){
            AmazonAdDTO ad = new AmazonAdDTO();
            ad.setType(RELATE_BRAND_AD);
            ad.setWebElement(adEle);

            String brand = "";
            try{
                WebElement infoElement = adEle.findElement(By.xpath(SEARCH_RESULT_BRAND_RELATE_AD_INFO_XPATH));

                String label = infoElement.getAttribute("aria-label");
                brand = label.substring(label.indexOf("from")+5, label.indexOf("."));

                ad.setBrand(brand);
            }catch (Exception ex){
                log.debug("[locateSponsoredProduct] Get brand relate ad brand name failed", ex);
            }

            sponsoredProductList.add(ad);
        }

        return sponsoredProductList;
    }

    /**
     * 定位详情页面中广告元素所在位置
     *
     * @param driver
     * @param blackMap
     * @param whiteMap
     * @return
     */
    private List<WebElement> locateDetailSponsoredProduct(WebDriver driver, HashMap<Object, Object> blackMap, HashMap<Object, Object> whiteMap) {
        // 返回过滤处理后的商品广告元素
        List<WebElement> sposoredProductList = new ArrayList<>();

        /** 详情页中商品广告主要存在位置，介绍下方，购物车下方，同类列表（暂时只测试第一页，后面页意义不大）
         *   1.//*[@id="ape_Detail_hero-quick-promo_Desktop_iframe"]
         *   2.//*[@id="ape_Detail_ams-detail-right-v2_desktop_iframe"]
         *   3.//*[@id="newer-version"]
         *   4.//*[@id="sims-consolidated-2_feature_div"]//ol
         */

        return sposoredProductList;
    }

    /**
     * 是否包含所含信息
     *
     * @param fullText
     * @param containsText
     * @return
     */
    private boolean containsInfo(String fullText, String containsText) {
        if (ObjectUtil.isEmpty(fullText) || ObjectUtil.isEmpty(containsText)) {
            return false;
        }
        return StrUtil.containsAnyIgnoreCase(fullText, containsText);
    }

    private boolean containsInfo(AmazonAdDTO amazonAd, String containsText) {

        if (amazonAd == null || StringUtils.isEmpty(containsText)) {
            return false;
        }

        if (StringUtils.isNotEmpty(amazonAd.getAsin()) && StrUtil.containsAnyIgnoreCase(amazonAd.getAsin(), containsText)) {
            return true;
        }

        if (StringUtils.isNotEmpty(amazonAd.getTitle()) && StrUtil.containsAnyIgnoreCase(amazonAd.getTitle(), containsText)) {
            return true;
        }

        return false;
    }

    private String getProductMainIntroduce(WebElement element, AmazonAdNodeType amazonAdNodeType) {
        String result = "";
        switch (amazonAdNodeType) {
            case SEARCH_RESULT_AD:
                result = commonFindInfoFunc(element, INDEX_CHILD_TEXT_XPATH, "alt", NodeTextGetTypeEnum.BY_ATTR);
                break;
            case DETAIL_AD:
                result = commonFindInfoFunc(element, ".", "title", NodeTextGetTypeEnum.BY_ATTR);
                break;
            case DETAIL_AD2:
                result = commonFindInfoFunc(element, ".", "", NodeTextGetTypeEnum.BY_TEXT);
                break;
            case BUYBOX_AD:
                break;
            case DETAIL_VERSION_AD:
                break;
            case SAME_PRODUCT_AD:
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * 主页商品元素下的商品标题文案定位
     *
     * @param element
     * @return
     */
    private String findIndexProductTitleInfo(WebElement element) {
        if (ObjectUtil.isNotEmpty(element.findElement(By.xpath(INDEX_CHILD_TEXT_XPATH)))) {
            return element.findElement(By.xpath(INDEX_CHILD_TEXT_XPATH)).getAttribute("alt").toString();
        } else {
            return "";
        }
    }

    /**
     * 文案定位公共方法
     *
     * @param element
     * @return
     */
    private String commonFindInfoFunc(WebElement element, String xpath, String attributeName, NodeTextGetTypeEnum getType) {
        String result = "";
        switch (getType) {
            case BY_TEXT:
                result = WebDriverUtils.isExistsElementFindByXpath(element, xpath) ? element.findElement(By.xpath(xpath)).getText() : "";
                break;
            case BY_ATTR:
                result = WebDriverUtils.isExistsElementFindByXpath(element, xpath) ? element.findElement(By.xpath(xpath)).getAttribute(attributeName) : "";
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * Process sponsored ad
     *
     * @param driver
     * @param element
     * @param amazonAdDTO
     * @param setting
     * @return
     */
    private int processSponsoredAd(WebDriver driver, WebElement element, AmazonAdDTO amazonAdDTO, AmazonAdConsumeSettingDTO setting) {

        if (driver == null || element == null || amazonAdDTO == null || setting == null){
            throw new IllegalArgumentException("Cannot process element cause the arguments have null object");
        }

        if(StringUtils.isEmpty(amazonAdDTO.getTitle()) && StringUtils.isEmpty(amazonAdDTO.getBrand()) && StringUtils.isEmpty(amazonAdDTO.getAsin())){
            log.debug("[processSponsoredAd] Ad {} have no detail cannot process", amazonAdDTO);
            return RespResult.NO_RECORD;
        }

        if(amazonAdDTO.getType() == SEARCH_RESULT_AD){
            // FIXME: Test
//            return RespResult.SUCC_OOM;
            return clickSearchResultAd(driver, element, amazonAdDTO, setting);
        }else if (amazonAdDTO.getType() == SEARCH_RESULT_INNER_ITEM_AD){
            // FIXME: Test
//            return RespResult.SUCC_OOM;
            return clickInnerItemAd(driver,element,amazonAdDTO,setting);
        }else if(amazonAdDTO.getType() == RELATE_BRAND_AD){
            return clickRelateBrandAd(driver,element,amazonAdDTO,setting);
        }else{
            log.debug("[redirectProductDetailByXpath] Ad type {} not support yet.", amazonAdDTO.getType());
            return RespResult.NO_RECORD;
        }
    }

    /**
     * Click search resutl item ad
     *
     * @param driver
     * @param element
     * @param amazonAdDTO
     * @param setting
     * @return
     */
    public int clickSearchResultAd(WebDriver driver, WebElement element, AmazonAdDTO amazonAdDTO, AmazonAdConsumeSettingDTO setting){
        try {

            WebElement imageEle = null;
            try{
                imageEle = element.findElement(By.xpath(SEARCH_RESULT_ITEMS_IMAGE_XPATH));
            }catch (Exception ex){
                log.debug("[redirectProductDetailByXpath] get sponsored item image failed");
            }
            WebElement textEle = null;

            try{
                textEle = element.findElement(By.xpath(SEARCH_RESULT_ITEMS_NAME_XPATH));
            }catch (Exception ex){
                log.debug("[redirectProductDetailByXpath] get sponsored item name failed");
            }

            if(imageEle == null && textEle == null){
                clickAd(driver, element, amazonAdDTO, () -> {
                    try{
                        productDetailProcess(driver, setting);
                    }catch (Exception e){
                        e.printStackTrace();
                        log.error(e.getMessage());
                    }
                });
            }else{
                if(imageEle == null){
                    clickAd(driver, textEle, amazonAdDTO, () -> {
                        try{
                            productDetailProcess(driver, setting);
                        }catch (Exception e){
                            e.printStackTrace();
                            log.error(e.getMessage());
                        }
                    });
                }else{
                    clickAd(driver, imageEle, amazonAdDTO, () -> {
                        try{
                            productDetailProcess(driver, setting);
                        }catch (Exception e){
                            e.printStackTrace();
                            log.error(e.getMessage());
                        }
                    });
                }
            }

            return RespResult.SUCC_OOM;
        } catch (Exception e) {
            log.error("[clickSearchResultAd] process search result item ad failed", e);
            return RespResult.FAILD;
        }
    }

    /**
     * Click inner ad
     *
     * @param driver
     * @param element
     * @param amazonAdDTO
     * @param setting
     * @return
     */
    public int clickInnerItemAd(WebDriver driver, WebElement element, AmazonAdDTO amazonAdDTO, AmazonAdConsumeSettingDTO setting){
        try{
            // find the link element that is not hidden
//            WebElement linkEle = element.findElement(By.xpath(".//a[not(@aria-hidden)]"));

            // Just click the link no further action
            // TODO: implement further action
            clickAd(driver, element, amazonAdDTO, null);

            return RespResult.SUCC_OOM;
        }catch (Exception e){
            log.error("[clickInnerAd] process inner ad failed", e);
            return RespResult.FAILD;
        }
    }

    /**
     * Click relate brand ad
     *
     * @param driver
     * @param element
     * @param amazonAdDTO
     * @param setting
     * @return
     */
    public int clickRelateBrandAd(WebDriver driver, WebElement element, AmazonAdDTO amazonAdDTO, AmazonAdConsumeSettingDTO setting){
        try{

            // Just click the link no further action
            clickAd(driver, element, amazonAdDTO, () -> {
                // TODO: implement further action
            });

            return RespResult.SUCC_OOM;
        }catch (Exception e){
            log.error("[clickRelateBrandAd] process inner ad failed", e);
            return RespResult.FAILD;
        }
    }

    /**
     * 跳转商品详情页面
     *
     * @param element
     */
    private int redirectProductDetail(WebElement element) {
        try {
            if (WebDriverUtils.isExistsElementFindByXpath(element, SEARCH_RESULT_ITEMS_IMAGE_XPATH) || WebDriverUtils.isExistsElementFindByXpath(element, SEARCH_RESULT_ITEMS_NAME_XPATH)) {

                WebElement nameEle = element.findElement(By.xpath(SEARCH_RESULT_ITEMS_NAME_XPATH));
                WebElement imageEle = element.findElement(By.xpath(SEARCH_RESULT_ITEMS_IMAGE_XPATH));

                if (nameEle == null){
                    imageEle.click();
                }

                if (imageEle == null){
                    nameEle.click();
                }

                // random click link title or image
                Random rand = new Random();
                int random = rand.nextInt(20);

                if (random % 3 == 0) {
                    nameEle.click();
                } else {
                    imageEle.click();
                }
                WebDriverUtils.randomSleep();
                return RespResult.SUCC_OOM;
            } else {
                return RespResult.NO_RECORD;
            }
        } catch (Exception e) {
            return RespResult.FAILD;
        }
    }


    /**
     * Process product detail page ad
     *
     * @param driver
     */
    private void productDetailProcess(WebDriver driver, AmazonAdConsumeSettingDTO setting) {

        log.debug("[productDetailProcess] process product detail page: {}, ad consume setting: {}", driver.getCurrentUrl(), setting.getId());

        // TODO:
        // 1.商品详情下方iframe逻辑
        proDetailIframeProcess(driver, setting);

        // 2.商品new版本逻辑

        // TODO:
        // 3.购物车下方iframe逻辑
        buyBoxIframeProcess(driver, setting);

        // TODO:
        // 4.Process Brand Relate Ad
        brandRelateAdProcess(driver, setting);

        // 5.广告列表框消耗逻辑

    }

    /**
     * 商品详情下方iframe逻辑
     * process the iframe under the product detail
     *
     * @param driver
     */
    private void proDetailIframeProcess(WebDriver driver, AmazonAdConsumeSettingDTO setting) {

        log.debug("[proDetailIframeProcess] process iframe ad under product detail");
        
    }


    /**
     * process the ad under buybox
     *
     * @param page
     * @param driver
     */
    private void buyBoxIframeProcess(Page page, WebDriver driver) {
        if (ObjectUtil.isNotEmpty(driver.findElement(By.xpath(BUYBOX_IFRAME_XPATH)))) {
            driver.switchTo().frame(driver.findElement(By.xpath(BUYBOX_IFRAME_XPATH)));
            if (ObjectUtil.isNotEmpty(driver.findElement(By.xpath(DETAIL_IFRAME_REDIRECT_XPATH)))) {
                WebElement iframeEle = driver.findElement(By.xpath(DETAIL_IFRAME_REDIRECT_XPATH));

                WebDriverUtils.isClicked(driver, iframeEle);

                // random sleep
                try {
                    WebDriverUtils.randomSleepBetween(5000, 10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                driver.navigate().back();
                driver.switchTo().defaultContent();
            } else {
                return;
            }
        } else {
            return;
        }
    }

    private void buyBoxIframeProcess(WebDriver driver, AmazonAdConsumeSettingDTO setting) {
        WebElement buyboxIframEle = WebDriverUtils.expWaitForElement(driver, By.xpath(BUYBOX_IFRAME_XPATH), 30);
        if (buyboxIframEle != null) {

        } else {
            return;
        }
    }

    /**
     * Process brand relate ad
     *
     * @param driver
     */
    private void brandRelateAdProcess(WebDriver driver, AmazonAdConsumeSettingDTO setting){
        List<WebElement> brandRelateEles = WebDriverUtils.expWaitForElements(driver, By.xpath(DETAIL_BRAND_RELATE_AD_XPATH), 30);

        if(brandRelateEles != null && brandRelateEles.size() > 0){

            for (WebElement ad: brandRelateEles){

                // TODO: check ad consume or not

            }
        }
    }

    private void clickAd(WebDriver driver, WebElement element, AmazonAdDTO amazonAd, Runnable func){

        log.debug("[clickAd] {}", amazonAd.toString());

        try{
            // TODO: debug
            try{
                WebDriverUtils.highlight(driver, element);
            }catch (Exception ex){
                log.debug("[clickAd] highlight failed", ex);
            }
            WebDriverUtils.randomSleepBetween(5000,8000);
            clickScrollAndBack(driver, element, amazonAd, func);
        }catch (Exception ex){
            ex.printStackTrace();
            log.error("[clickAd] failed", ex);
        }
    }

    /**
     * Click the element on page
     * stay at the page, scroll down and up
     * and navigate back
     */
    private void clickScrollAndBack(WebDriver driver, WebElement element){

        if(driver == null || element == null){
            throw new IllegalArgumentException("Cannot process page");
        }

        String home = driver.getCurrentUrl();

        try{
            element.click();
        }catch (Exception ex){
            ex.printStackTrace();
            return;
        }

        try {
            WebDriverUtils.randomSleepBetween(3000, 5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        JavascriptExecutor js = (JavascriptExecutor) driver;

        Random rand = new Random();

        int scrollDownCnt = rand.nextInt(5);

        int scrollUpCnt = rand.nextInt(scrollDownCnt);

        for (int i=0; i< scrollDownCnt; i++){
            js.executeScript("window.scrollBy(0,1000)");
        }

        try {
            WebDriverUtils.randomSleepBetween(3000, 5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i=0; i< scrollUpCnt; i++){
            js.executeScript("window.scrollBy(0,-1000)");
        }

        driver.get(home);

    }

    /**
     * Click the element on page
     * stay at the page, scroll down and up
     * Run the function
     * and navigate back
     */
    private void clickScrollAndBack(WebDriver driver, WebElement element, AmazonAdDTO amazonAd, Runnable func){

        log.debug("[clickScrollAndBack] element: {}", element);

        if(driver == null || element == null){
            throw new IllegalArgumentException("Cannot process page");
        }

        String home = driver.getCurrentUrl();

        try{
            boolean clicked = WebDriverUtils.isClicked(driver, element);
            if (!clicked){
                log.debug("[clickScrollAndBack] click not working");
                return;
            }else{
                logAmazonAdConsume(amazonAd);
            }
        }catch (Exception ex){
            ex.printStackTrace();
            return;
        }

        try{
            try {
                WebDriverUtils.randomSleepBetween(3000, 5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            JavascriptExecutor js = (JavascriptExecutor) driver;

            Random rand = new Random();

            int scrollDownCnt = rand.nextInt(5) + 1;

            int scrollUpCnt = rand.nextInt(scrollDownCnt);

            for (int i=0; i< scrollDownCnt; i++){
                js.executeScript("window.scrollBy(0,1000)");
            }

            try {
                WebDriverUtils.randomSleepBetween(3000, 5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(func != null){
                func.run();
            }

            for (int i=0; i< scrollUpCnt; i++){
                js.executeScript("window.scrollBy(0,-1000)");
            }

//            driver.navigate().back();
            driver.get(home);
        }catch (Exception ex){
            log.error("[clickScrollAndBack] ", ex);
//            driver.navigate().back();
            driver.get(home);
        }

    }

    /**
     * String数组转Map
     *
     * @param strList
     * @return
     */
    private Map<Object, Object> strListToMap(String[] strList) {
        Map<Object, Object> resultMap = new HashMap<>();
        for (int index = 0; index < strList.length; index++) {
            resultMap.put(strList[index], strList[index]);
        }
        return resultMap;
    }

    /**
     * Get black list from user setting
     *
     * @return
     */
    private List<AmazonAdConsumeItemDTO> getBlackList() {

        List<AmazonAdConsumeItemDTO> adBlackList = new ArrayList<>();

        adBlackList.add(AmazonAdConsumeItemDTO.builder().name("USB C").build());

        return adBlackList;

    }

    /**
     * Get white list from user setting
     *
     * @return
     */
    private List<AmazonAdConsumeItemDTO> getWhiteList() {

        List<AmazonAdConsumeItemDTO> adWhiteList = new ArrayList<>();

        adWhiteList.add(AmazonAdConsumeItemDTO.builder().name("SHey-bro").build());

        return adWhiteList;
    }



    /**
     * Log ad click when click the amazon ad
     *
     */
    private void logAmazonAdConsume(AmazonAdDTO amazonAd){

        CompletableFuture.runAsync(() -> {
            try{
                log.info("[logAmazonAdConsume] click ad {}", amazonAd.toString());

                amazonAdService.insertAdConsumeLog(amazonAd);

            }catch (Exception ex){
                log.error("[logAmazonAdConsume] log ad consume failed", ex);
            }
        });

    }

}

