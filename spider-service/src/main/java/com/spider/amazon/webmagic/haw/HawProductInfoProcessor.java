package com.spider.amazon.webmagic.haw;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.cons.DriverPathCons;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.model.HawSrapySkuInfoDO;
import com.spider.amazon.model.HawSrapySkuPropertyInfoDO;
import com.spider.amazon.remote.api.SpiderUrl;
import com.spider.amazon.utils.WebDriverUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

/**
 * Haw网站商品信息抓取
 */
@Component
@Slf4j
public class HawProductInfoProcessor implements PageProcessor {

    public static final String PRODUCT_ID_LIST = "PID_LIST";
    public static final String VENDOR_SKU_LIST = "VSKU_LIST";
    public static final String TASK_ID = "TASK_ID";
    public static final String M_ASIN_LIST ="MER_ASIN_LIST";
    public static final int GROUP_ALL = 0;
    public static final int FIRST_OBJ = 0;
    public static final int SPECS_NUM = 9;
    private int dataCount = 0;

    private Site site = Site
            .me()
            .setRetryTimes(3)
            .setDomain(SpiderUrl.SPIDER_HAW_INDEX)
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

        Map<String,Object> params=new HashMap<>();

        // 1.建立WebDriver
        System.setProperty("webdriver.chrome.driver", DriverPathCons.CHROME_DRIVER_PATH);
        WebDriver driver = new ChromeDriver();

        try {

            // 1.0隐式等待对象声明
            WebDriverWait wait = new WebDriverWait(driver, 30);

            // 1.1设置页面超时等待时间,20S
            driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

            // 2.初始打开页面
            driver.get(SpiderUrl.SPIDER_HAW_INDEX);

            // 3.输入文本框
            log.info("PRODUCT_ID_LIST=>[{}]  TASK_ID=>[{}] VENDOR_SKU_LIST=>[{}]", page.getRequest().getExtra(PRODUCT_ID_LIST).toString(), page.getRequest().getExtra(TASK_ID).toString() , page.getRequest().getExtra(VENDOR_SKU_LIST).toString());
            String productIdListStr = page.getRequest().getExtra(PRODUCT_ID_LIST).toString();
            String vSkuListStr=page.getRequest().getExtra(VENDOR_SKU_LIST).toString();
            String taskId = page.getRequest().getExtra(TASK_ID).toString();
            params.put("taskId",taskId);
            page.putField("taskId",taskId);
            String[] productIdList = page.getRequest().getExtra(PRODUCT_ID_LIST).toString().split("\\|");
            String[] vSkuList = page.getRequest().getExtra(VENDOR_SKU_LIST).toString().split("\\|");
            String[] mAsinList = page.getRequest().getExtra(M_ASIN_LIST).toString().split("\\|");

            //3.1构建映射
            Map<String,String> pIdToVskuMap=new HashMap<>();
            Map<String,String> pIdToMAsinMap=new HashMap<>();
            for (int pidListIndex=0;pidListIndex<productIdList.length;pidListIndex++) {
                pIdToVskuMap.put(productIdList[pidListIndex],vSkuList[pidListIndex]);
                pIdToMAsinMap.put(productIdList[pidListIndex],mAsinList[pidListIndex]);
            }

            // 4.定位文本框,循环输入文本,点击搜索按钮
            List<HawSrapySkuInfoDO> hawSrapySkuInfoDOList=new ArrayList<>();
            List<List<HawSrapySkuPropertyInfoDO>> hawSrapySkuPropertyInfoDOList=new ArrayList<>();
            for (String curProductId : productIdList
                    ) {
                params.remove("curProductId");
                params.put("curProductId",curProductId);
                log.info("curProductId=>",curProductId);
                // 输入文本框
                WebElement inputText = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='searchTermInput']"), 10);
                inputText.sendKeys(curProductId);
                // 点击查询按钮
                WebElement searchButton = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='searchSubmitButton']"), 10);
                searchButton.click();
                // 查询结果是否存在
                if (!WebDriverUtils.isExistsElementFindByXpath(driver,By.xpath("//tr[contains(@id,'" + curProductId + "')]//a[contains(@href,'/shop/product')][1]"),5)) {
                    continue;
                }
                // 切换Brands标签
                WebElement brandsButton = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='part-list-container']//ul/li[2]/a"), 10);
                brandsButton.click();
                // 获取品牌信息
                getProductBrands(page,driver,params);
                // 切换Products标签
                WebElement productsButton = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='part-list-container']//ul/li[1]/a"), 10);
                productsButton.click();
                // 点击详情跳转按钮
                WebElement redirectAddr = WebDriverUtils.expWaitForElement(driver, By.xpath("//tr[contains(@id,'" + curProductId + "')]//a[contains(@href,'/shop/product')][1]"), 10);
                if (ObjectUtil.isEmpty(redirectAddr)) {
                    continue;
                }
                redirectAddr.click();
                // 获取页面各元素信息
                // 获取详情
                getProductInfo(page,driver,params,hawSrapySkuInfoDOList,pIdToVskuMap,pIdToMAsinMap);

                // 获取属性列表
                getProductListInfo(page,driver,params,hawSrapySkuPropertyInfoDOList,pIdToVskuMap,pIdToMAsinMap);

                dataCount++;
                log.info("第 [{}] 次数据,hawSrapySkuInfoDOList=>[{}] hawSrapySkuPropertyInfoDOList=>[{}] ",dataCount,hawSrapySkuInfoDOList,hawSrapySkuPropertyInfoDOList);

            }
            log.info("hawSrapySkuInfoDOList=>[{}] ",hawSrapySkuInfoDOList);
            page.putField("hawSrapySkuInfoDOList",hawSrapySkuInfoDOList);
            log.info("hawSrapySkuPropertyInfoDOList=>[{}] ",hawSrapySkuPropertyInfoDOList);
            page.putField("hawSrapySkuPropertyInfoDOList",hawSrapySkuPropertyInfoDOList);

            try {
                sleep(30000);
            } catch (InterruptedException e) {
                throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), RespErrorEnum.SPIDER_EXEC.getSubStatusMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(RespErrorEnum.SPIDER_EXEC.getSubStatusCode(), e.getMessage());
        } finally {
            driver.quit();
        }

        if (log.isInfoEnabled()) {
            log.info("1.step84=>抓取结束");
        }
    }

    /**
     * 获取商品主要属性数据
     *
     * @param page
     */
    private void getProductInfo(Page page, WebDriver driver, Map<String,Object> params , List<HawSrapySkuInfoDO> hawSrapySkuInfoDOList ,Map<String,String> pIdToVskuMap,Map<String,String> pIdToMAsinMap) {
        log.info(" 获取商品主要属性数据 ");

        // 获取主要数据
        // Pid
        WebElement ele1 = WebDriverUtils.expWaitForElement(driver, By.xpath("//tr[contains(@id,'" + params.get("curProductId") + "')]//*[@id='partIdTxt']"), 10);
        String productId=ObjectUtil.isNotEmpty(ele1)? ele1.getText():"";
        // PSimpleId
        WebElement ele2 = WebDriverUtils.expWaitForElement(driver, By.xpath("//tr[contains(@id,'" + params.get("curProductId") + "')]//*[@id='partIdTxt']/../em"), 10);
        String productSimpleId=ObjectUtil.isNotEmpty(ele2)? ele2.getText():"";
        // 产品标题
        WebElement ele3 = WebDriverUtils.expWaitForElement(driver, By.xpath("//tr[contains(@id,'" + params.get("curProductId") + "')]//*[@itemprop='name']"), 10);
        String productTitle=ObjectUtil.isNotEmpty(ele3)? ele3.getText():"";
        // 产品额外标题
        WebElement ele4 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='imgPartContainer-outer']//div[contains(@class,'"+params.get("curProductId")+"')]//img"), 10);
        String productTitleElse=ObjectUtil.isNotEmpty(ele4)? ele4.getAttribute("alt"):"";
        // 产品价格
        WebElement ele5 =WebDriverUtils.expWaitForElement(driver, By.xpath("//tr[contains(@id,'" + params.get("curProductId") + "')]/td[3]/div[2]/div[1]"), 10);
        String productPrice=ObjectUtil.isNotEmpty(ele5)? ele5.getText():"";
        // 图片路径
        WebElement ele6 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='imgPartContainer-outer']//div[contains(@class,'"+params.get("curProductId")+"')]//img"), 10);
        String imgUrl=ObjectUtil.isNotEmpty(ele6)? ele6.getAttribute("src"):"";
        // 产品介绍
        WebElement ele7 = WebDriverUtils.expWaitForElement(driver, By.xpath("//*[@id='partLongDescText']/div[1]"), 10);
        String productIntroduce=ObjectUtil.isNotEmpty(ele7)? ele7.getText():"";
        String pageUrl=driver.getCurrentUrl();
        String vendorSku=pIdToVskuMap.get(params.get("curProductId"));
        String merchantSuggestedAsin=pIdToMAsinMap.get(params.get("curProductId"));
        String brands= String.valueOf(params.get("curBrands"));
        log.info("productId=>[{}] productSimpleId=>[{}] productTitle=>[{}] productTitleElse=>[{}]",productId,productSimpleId,productTitle,productTitleElse);
        log.info("productPrice=>[{}] imgUrl=>[{}] productIntroduce=>[{}] pageUrl=>[{}] vendorSku=[{}] merchantSuggestedAsin=>[{}] brands=>[{}]",productPrice,imgUrl,productIntroduce,pageUrl,vendorSku,merchantSuggestedAsin,brands);

        // 构建实体
        HawSrapySkuInfoDO hawSrapySkuInfoDO=HawSrapySkuInfoDO.builder()
                .taskId(ObjectUtil.toString(params.get("taskId")))
                .productId(StrUtil.strip(productId," "))
                .productSimpleId(CollUtil.isEmpty(ReUtil.findAll("(?<=\\()(.+?)(?=\\))",productSimpleId,GROUP_ALL))?"": ReUtil.findAll("(?<=\\()(.+?)(?=\\))",productSimpleId,GROUP_ALL).get(FIRST_OBJ))
                .productTitle(productTitle)
                .productTitleElse(productTitleElse)
                .productPrice(productPrice)
                .imgUrl(imgUrl)
                .productIntroduce(productIntroduce)
                .pageUrl(pageUrl)
                .insertTime(DateUtil.date())
                .vendorSku(vendorSku)
                .merchantSuggestedAsin(merchantSuggestedAsin)
                .productBrands(brands)
                .build();
        log.info("hawSrapySkuInfoDO=>[{}]",hawSrapySkuInfoDO);

        hawSrapySkuInfoDOList.add(hawSrapySkuInfoDO);
    }

    /**
     * 获取商品属性列表数据
     *
     * @param page
     */
    private void getProductListInfo(Page page, WebDriver driver,Map<String,Object> params,List<List<HawSrapySkuPropertyInfoDO>> hawSrapySkuPropertyInfoDOList,Map<String,String> pIdToVskuMap,Map<String,String> pIdToMAsinMap) {
        log.info(" 获取商品属性列表信息 ");

        // 获取表格
        Html html= new Html(driver.getPageSource());
        Elements tableElement= html.getDocument().getElementById("Specs").getElementsByTag("table");
        Elements trs= tableElement.tagName("tbody").select("tr");
        // 提取元素数据
        String productId=WebDriverUtils.expWaitForElement(driver, By.xpath("//tr[contains(@id,'" + params.get("curProductId") + "')]//*[@id='partIdTxt']"), 10).getText();
        String productSimpleId=ObjectUtil.isNotEmpty(WebDriverUtils.expWaitForElement(driver, By.xpath("//tr[contains(@id,'" + params.get("curProductId") + "')]//*[@id='partIdTxt']/../em"), 10))?  WebDriverUtils.expWaitForElement(driver, By.xpath("//tr[contains(@id,'" + params.get("curProductId") + "')]//*[@id='partIdTxt']/../em"), 10).getText():"";
        String vendorSku=pIdToVskuMap.get(params.get("curProductId"));
        String merchantSuggestedAsin=pIdToMAsinMap.get(params.get("curProductId"));
        boolean hasSpecsFlg = false;
        int listIndex=0;
        List<HawSrapySkuPropertyInfoDO> propertiesList= new ArrayList<>();
        for (int trindex=0;trindex<trs.size();++trindex) {
            if (hasSpecsFlg==true && listIndex<SPECS_NUM) {
                if (listIndex!=0 ) { // 过滤非属性列表行
                    Element tr = trs.get(trindex);
                    // 获取属性名，属性值
                    Elements tds = tr.select("td");
                    HawSrapySkuPropertyInfoDO hawSrapySkuPropertyInfoDO=HawSrapySkuPropertyInfoDO.builder()
                            .taskId(ObjectUtil.toString(params.get("taskId")))
                            .productId(StrUtil.strip(productId," "))
                            .productSimpleId(CollUtil.isEmpty(ReUtil.findAll("(?<=\\()(.+?)(?=\\))",productSimpleId,GROUP_ALL))?"": ReUtil.findAll("(?<=\\()(.+?)(?=\\))",productSimpleId,GROUP_ALL).get(FIRST_OBJ))
                            .propertyName(tds.get(0).text())
                            .propertyValue(tds.get(1).text())
                            .insertTime(DateUtil.date())
                            .vendorSku(vendorSku)
                            .merchantSuggestedAsin(merchantSuggestedAsin)
                            .build();
                    propertiesList.add(hawSrapySkuPropertyInfoDO);
                }
                ++listIndex;
            }
            if (ObjectUtils.isNotEmpty(trs.get(trindex).select("td")) && StrUtil.containsIgnoreCase(trs.get(trindex).select("td").get(0).text(),ObjectUtil.toString(params.get("curProductId")))) {
                hasSpecsFlg=true;
                hawSrapySkuPropertyInfoDOList.add(propertiesList);
            }
        }
        log.info("hawSrapySkuPropertyInfoDOList=>[{}]",hawSrapySkuPropertyInfoDOList);

    }

    /**
     * 获取产品品牌信息
     * @param page
     * @param driver
     * @param params
     */
    private void getProductBrands(Page page, WebDriver driver,Map<String,Object> params) {
        WebElement element=WebDriverUtils.expWaitForElement(driver,By.xpath("//*[@id='brand-list-container']//h4"),10);
        String brands="";
        if (ObjectUtil.isNotEmpty(element)) {
            brands=element.getText().trim();
        }
        params.remove("curBrands");
        params.put("curBrands",brands);
    }

    public static void main(String[] args) {
        System.out.println("0.step67=>抓取程序开启。");

//        Spider.create(new HawProductInfoProcessor())
//                .addUrl(SpiderUrl.SPIDER_HAW_INDEX)
//                .run();

        String temp="HGC736472|HGC701265";
        String[] productIdList = temp.split("\\|");
        System.out.println("productIdList =>" + productIdList[0]);

        System.out.println("end.step93=>抓取程序结束。");

    }

}

