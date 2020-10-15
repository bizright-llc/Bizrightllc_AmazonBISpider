package com.spider.amazon.webmagic.amz;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.cons.*;
import com.spider.amazon.dto.AmazonAdIndexDTO;
import com.spider.amazon.remote.api.SpiderUrl;
import com.spider.amazon.utils.WebDriverUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.Proxy;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.spider.amazon.cons.AdNodeType.INDEX_AD;
import static java.lang.Thread.sleep;

/**
 * Amazon广告消耗功能
 * TODO DEMO暂未进行功能解耦
 */
@Component
@Slf4j
public class AmazonAdConsumeProcessor implements PageProcessor {

    // 关键词列表,在输入框循环输入进行搜索的关键词,example:xxx|xxx|xxx|
    public final static String SEARCH_LIST="SEARCH_LIST";
    // 广告黑名单,在黑名单上会进行广告消耗逻辑
    public final static String BLACK_LIST="BLACK_LIST";
    // 广告白名单，在白名单上的关键词商品不会进行消耗逻辑
    public final static String WHITE_LIST="WHITE_LIST";
    public final static String LOCATION="10001";

    // Xpath列表
    // location xpath
    public final static String LOCATION_XPATH="//*[@id='glow-ingress-line2' and contains(text(),'10001')]"; // Location 位置以LA位置为准
    public final static String LOCATION_CLICK="//DIV[@id='nav-packard-glow-loc-icon']"; // 定位点击
    public final static String LOCATION_CHANGE_BUTTON="//*[@id='GLUXChangePostalCodeLink']"; // 定位改变按钮
    public final static String LOCATION_INPUT_TEXT="//*[@id='GLUXZipUpdateInput']"; // 定位信息输入框
    public final static String LOCATION_INPUT_CHECK="//*[@id='GLUXZipUpdate-announce']/../input"; // 定位信息确认
    public final static String LOCATION_INPUT_DOWN="//*[@name='glowDoneButton']"; // 定位信息DOWN
    public final static String LOCATION_INPUT_CONTINUE="//*[@id='GLUXConfirmClose']"; // 定位信息刷新按钮
    //主页广告位置
    public final static String SEARCH_INPUT_ELE="//*[@id='twotabsearchtextbox']"; // 搜索框输入框
    public final static String SEARCH_CLICK_ELE="//*[@id='nav-search-submit-text']/../input"; // 搜索框搜索按钮
    public final static String INDEX_SPONSORED_XPATH="//*[@id='search']//div[contains(@class,'s-search-results')]/div[@data-index!='']"; // 搜索页广告元素xpath
    public final static String INDEX_SPONSORED_REXPATH="//*[@id='search']//div[contains(@class,'s-search-results')]/div[@data-index='{dataIndex}']"; // 搜索页广告元素xpath
    public final static String INDEX_CHILD_TEXT_XPATH=".//span[@cel_widget_id='SEARCH_RESULTS-SEARCH_RESULTS']//img"; // 广告元素子节点TEXT，用于筛选是否是黑白名单
    public final static String INDEX_ISSPONSORED_XPATH=".//span[text()='Sponsored']"; // 广告元素子节点TEXT，用于筛选是否是广告商品
    public final static String REDIRECT_DETAIL_XPATH=".//span[@cel_widget_id='SEARCH_RESULTS-SEARCH_RESULTS']//img/../../../a[1]"; // 广告元素跳转详情页面链接地址元素
    // 详情页广告商品出现位置xpath
    public final static String DETAIL_IFRAME_XPATH="//*[@id='ape_Detail_hero-quick-promo_Desktop_iframe']"; // 详情下方iframe
    public final static String DETAIL_IFRAME_REDIRECT_XPATH=".//*[@id='sp_hqp_shared_inner']/div/a"; // 详情下方iframe重定向地址
    public final static String DETAIL_IFRAME_TEXT_XPATH=".//*[@id='sp_hqp_shared_inner']/div/a"; // 详情下方iframe过滤文案
    public final static String DETAIL_IFRAME_TEXT_XPATH2=".//a[@id='title']"; // 详情下方iframe过滤文案
    // 购物车下方广告商品出现位置xpath
    public final static String BUYBOX_IFRAME_XPATH="//*[@id='ape_Detail_ams-detail-right-v2_desktop_iframe']"; // 购物车下方iframe
    public final static String BUYBOX_IFRAME_REDIRECT_XPATH=".//*[@id='ape_Detail_ams-detail-right-v2_desktop_iframe']"; // 购物车下方iframe重定向地址
    public final static String BUYBOX_IFRAME_TEXT_XPATH=".//*[@id='ape_Detail_ams-detail-right-v2_desktop_iframe']"; // 购物车下方iframe过滤文案
    // 详情页新版本商品位置xpath
    public final static String DETAIL_VERSION_XPATH="//*[@id='newer-version']"; // 产品系列新版本
    public final static String DETAIL_VERSION_REDIRECT_XPATH=".//*[@id='newer-version']"; // 产品系列新版本
    public final static String DETAIL_VERSION_TEXT_XPATH=".//*[@id='newer-version']"; // 产品系列新版本
    // 同类型广告产品列表位置xpath
    public final static String SAME_PRODUCT_XPATH="//*[@id='sims-consolidated-2_feature_div']//ol"; // 同类型广告产品列表
    public final static String SAME_PRODUCT_REDIRECT_XPATH=".//*[@id='sims-consolidated-2_feature_div']//ol"; // 同类型广告产品列表
    public final static String SAME_PRODUCT_TEXT_XPATH=".//*[@id='sims-consolidated-2_feature_div']//ol"; // 同类型广告产品列表

    private SpiderConfig spiderConfig;

    private Site site = Site
            .me()
            .setRetryTimes(3)
            .setDomain(SpiderUrl.AMAZON_INDEX)
            .setSleepTime(3000)
            .setUserAgent(
                    "User-Agent:Mozilla/5.0(Macintosh;IntelMacOSX10_7_0)AppleWebKit/535.11(KHTML,likeGecko)Chrome/17.0.963.56Safari/535.11");

    @Autowired
    public AmazonAdConsumeProcessor(SpiderConfig spiderConfig) {
        this.spiderConfig = spiderConfig;
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

        Map<String,Object> params=new HashMap<>();

        // 1.建立WebDriver
        System.setProperty("webdriver.chrome.driver", spiderConfig.getChromeDriverPath());
        WebDriver driver = new ChromeDriver();

        try {

            // 1.0设置页面超时等待时间,5S
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

            // 2.初始打开页面
            driver.get(SpiderUrl.AMAZON_INDEX);
            log.info("Headers=>[{}]",page.getRequest());

            // 2.1 切换定位，现本地抓取IP默认为中国，文案都是中文
            changeLocation(driver,page);

            // 3.当前搜索框循环输入参数列表
            // 3.0
            // TODO test
            String searchListStr ="18 Inch High Velocity 3 Speed for Industrial|";
            String blackListStr ="Simple Deluxe 18 Inch High Velocity|";
            String whiteListStr ="Hey-bro|";

//            String searchListStr =page.getRequest().getExtra(SEARCH_LIST).toString();
//            String blackListStr =page.getRequest().getExtra(BLACK_LIST).toString();
//            String whiteListStr =page.getRequest().getExtra(WHITE_LIST).toString();
            String[] searchList=searchListStr.split("\\|");
            String[] blackList=blackListStr.split("\\|");
            String[] whiteList=whiteListStr.split("\\|");
            Map<Object, Object> blackMap= strListToMap(blackList);
            Map<Object, Object> whiteMap= strListToMap(whiteList);


            // 3.1 定位输入框输入当前循环参数（外层循环）
            for (int searchIndex=0;searchIndex<searchList.length;searchIndex++) {
                if (isExistsSearchBox(driver)) {  // 存在搜索框
//                    driver.get(SpiderUrl.AMAZON_INDEX);
                    WebElement searchElement= WebDriverUtils.expWaitForElement(driver, By.xpath(SEARCH_INPUT_ELE), 10);
                    WebElement searchClickElement = WebDriverUtils.expWaitForElement(driver, By.xpath(SEARCH_CLICK_ELE), 10);
                    searchElement.sendKeys(searchList[searchIndex]);
                    searchClickElement.click();

                    // 4.定位Sponsored广告商品
                    List<AmazonAdIndexDTO> sponsoredProductList=locateSponsoredProduct( driver, blackMap, whiteMap);
                    // 4.1收集元素xpath，刷新页面，回退页面，元素会失效，需要重新定位

                    // 4.1黑白名单过滤广告商品，过滤后需攻击商品进入消耗列表
                    // TODO 该部分消耗列表使用schedule，或是直接点击进入待确定
                    // 循环点击进入商品详情页
                    for (AmazonAdIndexDTO sponsoredProduct:sponsoredProductList) {

                        WebElement productElement = null;
                        if (!isSponsoredPro(driver,sponsoredProduct,blackMap,whiteMap)) {
                            continue;
                        }

                        // 点击跳转进入商品页
                        productElement=driver.findElement(By.xpath(INDEX_SPONSORED_REXPATH.replace("{dataIndex}",sponsoredProduct.getDataIndex())));
                        int result = redirectProductDetailByXpath(driver,productElement);
                        if (result!=RespResult.SUCC_OOM) {
                            continue;
                        }

                        // 商品详情页面进行操作
                        productDetailProcess(page,driver,blackMap,whiteMap);

                        // 商品页面回退
                        driver.navigate().back();
                    }

                } else {
                    break;
                }
            }


            // 5.进入广告商品详情页面后
            // 广告商品详情页背后面

            // 5.1 定位详情页面广告位置，iframe等

            // 5.2 黑白名单确认是否需要进行点击进入
            // TODO 点击后回退页面等问题待确定，上下访问页面切换



            try {
                sleep(30000);
            } catch (InterruptedException e) {
                throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
        } finally {
            driver.quit();
        }

        if (log.isInfoEnabled()) {
            log.info("1.step84=>抓取结束");
        }

    }

    private boolean isSponsoredPro(WebDriver driver,AmazonAdIndexDTO object,Map<Object, Object> blackMap, Map<Object, Object> whiteMap) {
        String xpath=INDEX_SPONSORED_REXPATH.replace("{dataIndex}",object.getDataIndex());
        WebElement element=null;
        if(WebDriverUtils.isExistsElementFindByXpath(driver, By.xpath(xpath),5)) {
            element=driver.findElement(By.xpath(xpath));
        }
        if (isBlack(element, INDEX_AD,blackMap) && isNoWhite(element, INDEX_AD,whiteMap) && isSponsored(element)) {
           return true;
        }
        return false;
    }

    private void changeLocation(WebDriver driver,Page page) {

        log.info("Change Location");

        // 1.查找定位标签
        WebElement locationElement = WebDriverUtils.expWaitForElement(driver, By.xpath(LOCATION_XPATH), 10);
        if (ObjectUtil.isNotEmpty(locationElement)) { // 已经切换至目标区域,中断返回
            return;
        }

        // 2.点击定位切换
        WebElement locationClick = WebDriverUtils.expWaitForElement(driver, By.xpath(LOCATION_CLICK), 10);
        if (ObjectUtil.isNotEmpty(locationClick)) {
            locationClick.click();
        }

        // 3.查找change按钮
        WebElement changeElement = WebDriverUtils.expWaitForElement(driver, By.xpath(LOCATION_CHANGE_BUTTON), 10);
        if (ObjectUtil.isNotEmpty(changeElement)) { // 包含change按钮，先进行点击change按钮操作
            changeElement.click();
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
     * @param driver
     * @return
     */
    private boolean isExistsSearchBox(WebDriver driver) {
        log.info("step [isExistsSearchBox]");
        WebElement element = WebDriverUtils.expWaitForElement(driver, By.xpath(SEARCH_INPUT_ELE), 10);
        WebElement clickElement = WebDriverUtils.expWaitForElement(driver, By.xpath(SEARCH_CLICK_ELE), 10);
        if (ObjectUtil.isEmpty(element)||ObjectUtil.isEmpty(clickElement)) {  // 不存在搜索框
            return false;
        }
        return  true;
    }

    /**
     * 定位搜索主页需要消耗广告的商品列表元素
     * @param driver
     * @param blackMap
     * @param whiteMap
     * @return
     */
    private List<AmazonAdIndexDTO> locateSponsoredProduct(WebDriver driver, Map<Object, Object> blackMap, Map<Object, Object> whiteMap) {
        // 返回过滤处理后的商品广告元素
        List<AmazonAdIndexDTO> sposoredProductList=new ArrayList<>();
        // 获取当前搜索页所有搜索产品标签格
        List<WebElement> productElementList = driver.findElements(By.xpath(INDEX_SPONSORED_XPATH));
        // 遍历产品元素，黑白名单筛选需要进一步点击的商品
        for (WebElement productElement:productElementList) {
            log.debug("asin=>[{}]",productElement.getAttribute("data-asin"));
            // 黑白名单筛选
//            if (isBlack(productElement, INDEX_AD,blackMap) && isNoWhite(productElement, INDEX_AD,whiteMap) && isSponsored(productElement)) {
//                sposoredProductList.add(AmazonAdIndexDTO.builder()
//                        .dataAsin(productElement.getAttribute("data-asin"))
//                        .dataIndex(productElement.getAttribute("data-index"))
//                        .build());
//            }
            sposoredProductList.add(AmazonAdIndexDTO.builder()
                    .dataAsin(productElement.getAttribute("data-asin"))
                    .dataIndex(productElement.getAttribute("data-index"))
                    .build());
        }
        return sposoredProductList;
    }

    /**
     * 定位详情页面中广告元素所在位置
     * @param driver
     * @param blackMap
     * @param whiteMap
     * @return
     */
    private List<WebElement> locateDetailSponsoredProduct(WebDriver driver, HashMap<Object, Object> blackMap, HashMap<Object, Object> whiteMap) {
        // 返回过滤处理后的商品广告元素
        List<WebElement> sposoredProductList=new ArrayList<>();

        /** 详情页中商品广告主要存在位置，介绍下方，购物车下方，同类列表（暂时只测试第一页，后面页意义不大）
         *   1.//*[@id="ape_Detail_hero-quick-promo_Desktop_iframe"]
         *   2.//*[@id="ape_Detail_ams-detail-right-v2_desktop_iframe"]
         *   3.//*[@id="newer-version"]
         *   4.//*[@id="sims-consolidated-2_feature_div"]//ol
         */

        return sposoredProductList;
    }

    /**
     * 判断是否在黑名单中，在黑名单中进行消耗
     * @param element
     * @param adNodeType
     * @param blackMap
     * @return
     */
    private boolean isBlack(WebElement element, AdNodeType adNodeType, Map<Object, Object> blackMap) {
        String key=null;
        String value=null;
        Iterator blackMapIte = blackMap.entrySet().iterator();
        while (blackMapIte.hasNext()) {
            Map.Entry entry = (Map.Entry) blackMapIte.next();
            key = (String) entry.getKey();
            value = (String) entry.getValue();
            // 判断商品表意是否在黑名单里面
            if (containsInfo(getProductMainIntroduce(element,adNodeType),key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否包含所含信息
     * @param fullText
     * @param containsText
     * @return
     */
    private boolean containsInfo(String fullText, String containsText) {
        if (ObjectUtil.isEmpty(fullText) || ObjectUtil.isEmpty(containsText)) {
            return false;
        }
        return StrUtil.containsAnyIgnoreCase(fullText,containsText);
    }

    /**
     * 判断是否在白名单中，在白名单中不进行消耗
     * @param element
     * @param adNodeType
     * @param whiteMap
     * @return
     */
    private boolean isNoWhite(WebElement element, AdNodeType adNodeType, Map<Object, Object> whiteMap) {
        String key=null;
        String value=null;
        Iterator whiteMapIte = whiteMap.entrySet().iterator();
        while (whiteMapIte.hasNext()) {
            Map.Entry entry = (Map.Entry) whiteMapIte.next();
            key = (String) entry.getKey();
            value = (String) entry.getValue();
            // 判断商品表意是否在白名单里面
            if (containsInfo(getProductMainIntroduce(element,adNodeType),key)) {
                return false;
            }
        }
        return true;
    }

    private String getProductMainIntroduce(WebElement element,AdNodeType adNodeType) {
        String result="";
        switch (adNodeType) {
            case INDEX_AD:
                result=commonFindInfoFunc(element,INDEX_CHILD_TEXT_XPATH,"alt",NodeTextGetTypeEnum.BY_ATTR);
                break;
            case DETAIL_AD:
                result=commonFindInfoFunc(element,".","title",NodeTextGetTypeEnum.BY_ATTR);
                break;
            case DETAIL_AD2:
                result=commonFindInfoFunc(element,".","",NodeTextGetTypeEnum.BY_TEXT);
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
     * 判断是否有广告标志
     * @param element
     * @return
     */
    private boolean isSponsored(WebElement element) {
        return WebDriverUtils.isExistsElementFindByXpath(element,INDEX_ISSPONSORED_XPATH);
    }

    /**
     * 跳转商品详情页面
     * @param driver
     * @param element
     */
    private int redirectProductDetailByXpath(WebDriver driver,WebElement element) {
        try {
            return redirectProductDetail(element);
        } catch (Exception e) {
            return RespResult.FAILD;
        }
    }

    /**
     * 跳转商品详情页面
     * @param element
     */
    private int redirectProductDetail(WebElement element) {
        try {
            if (WebDriverUtils.isExistsElementFindByXpath(element,REDIRECT_DETAIL_XPATH)) {
                element.findElement(By.xpath(REDIRECT_DETAIL_XPATH)).click();
                return RespResult.SUCC_OOM;
            } else {
                return RespResult.NO_RECORD;
            }
        } catch (Exception e) {
            return RespResult.FAILD;
        }
    }



    /**
     * 详情页面消耗广告逻辑主流程
     * @param page
     * @param driver
     */
    private void  productDetailProcess(Page page,WebDriver driver,Map<Object, Object> blackMap, Map<Object, Object> whiteMap) {

        // 1.商品详情下方iframe逻辑
        proDetailIframeProcess(page,driver,blackMap, whiteMap);

        // 2.商品new版本逻辑


        // 3.购物车下方iframe逻辑


        // 4.广告列表框消耗逻辑

    }

    /**
     * 商品详情下方iframe逻辑
     * @param page
     * @param driver
     * @param blackMap
     * @param whiteMap
     */
    private void  proDetailIframeProcess(Page page,WebDriver driver,Map<Object, Object> blackMap, Map<Object, Object> whiteMap) {

        if (WebDriverUtils.isExistsElementFindByXpath(driver,By.xpath(DETAIL_IFRAME_XPATH),3)) { // 切换iframe
            driver.switchTo().frame(driver.findElement(By.xpath(DETAIL_IFRAME_XPATH)));
        } else {
            return;
        }

        WebElement textElement=null;
        AdNodeType adNodeType = null;
        if (WebDriverUtils.isExistsElementFindByXpath(driver,By.xpath(DETAIL_IFRAME_TEXT_XPATH),3)) { // Text节点1
            textElement = driver.findElement(By.xpath(DETAIL_IFRAME_TEXT_XPATH));
            adNodeType=AdNodeType.DETAIL_AD;
        }

        if (WebDriverUtils.isExistsElementFindByXpath(driver,By.xpath(DETAIL_IFRAME_TEXT_XPATH2),3)) { // Text节点2
            textElement = driver.findElement(By.xpath(DETAIL_IFRAME_TEXT_XPATH2));
            adNodeType=AdNodeType.DETAIL_AD2;
        }
        if (ObjectUtil.isNotEmpty(textElement) && isBlack(textElement, adNodeType,blackMap) && isNoWhite(textElement,adNodeType,whiteMap) ) {
            log.debug("textElement=>[{}]",textElement.getAttribute("href"));
            textElement.click();
            // TODO 跳转后随机停留几秒
            driver.navigate().back();
            driver.switchTo().defaultContent();
        }
    }


    /**
     * 购物车下方iframe逻辑
     * @param page
     * @param driver
     */
    private void  buyBoxIframeProcess(Page page,WebDriver driver) {
        if (ObjectUtil.isNotEmpty(driver.findElement(By.xpath(BUYBOX_IFRAME_XPATH)))) {
            driver.switchTo().frame(driver.findElement(By.xpath(BUYBOX_IFRAME_XPATH)));
            if (ObjectUtil.isNotEmpty(driver.findElement(By.xpath(DETAIL_IFRAME_REDIRECT_XPATH)))) {
                driver.findElement(By.xpath(DETAIL_IFRAME_REDIRECT_XPATH)).click();
                // TODO 跳转后随机停留几秒
                driver.navigate().back();
                driver.switchTo().defaultContent();
            } else {
                return;
            }
        } else {
            return;
        }
    }

    /**
     * String数组转Map
     * @param strList
     * @return
     */
    private Map<Object,Object> strListToMap(String [] strList) {
        Map <Object,Object> resultMap=new HashMap<>();
        for (int index=0;index<strList.length;index++)  {
            resultMap.put(strList[index],strList[index]);
        }
        return resultMap;
    }

    /**
     * 不错的免费代理IP站点
     * www.89ip.cn
     *
     * @return
     */
    public static List<Proxy> buildProxyIP() throws IOException {
        List<Proxy> proxies = new ArrayList<Proxy>();
        proxies.add(new Proxy("210.22.5.117", Integer.valueOf("3128")));
        proxies.add(new Proxy("47.99.65.77", Integer.valueOf("3128")));
        return proxies;
    }



}

