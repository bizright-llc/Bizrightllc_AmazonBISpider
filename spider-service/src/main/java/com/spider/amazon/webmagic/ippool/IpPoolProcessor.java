package com.spider.amazon.webmagic.ippool;

import cn.hutool.core.date.DateUtil;
import com.common.exception.ServiceException;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.cons.RandomUserAgent;
import com.spider.amazon.cons.RespErrorEnum;
import com.spider.amazon.dto.ProxyDTO;
import com.spider.amazon.model.ProxyDO;
import com.spider.amazon.remote.api.SpiderUrl;
import com.spider.amazon.service.ProxyService;
import com.spider.amazon.utils.WebDriverUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

/**
 * 获取IP代理池
 */
@Component
@Slf4j
public class IpPoolProcessor implements PageProcessor {

    private SpiderConfig spiderConfig;

    private ProxyService proxyService;

    private Site site = Site
            .me()
            .setRetryTimes(3)
            .setDomain(SpiderUrl.IP_POOL_INDEX)
            .setSleepTime(3000)
            .setUserAgent(RandomUserAgent.getRandomUserAgent());

    @Autowired
    public IpPoolProcessor(SpiderConfig spiderConfig, ProxyService proxyService) {
        this.spiderConfig = spiderConfig;
        this.proxyService = proxyService;
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
            log.info("Url [{}]",page.getRequest().getUrl());
        }


        Map<String,Object> params=new HashMap<>();

        // 1.建立WebDriver
        System.setProperty("webdriver.chrome.driver", spiderConfig.getChromeDriverPath());
        WebDriver driver = new ChromeDriver();

        try {

            // 1.0隐式等待对象声明
            WebDriverWait wait = new WebDriverWait(driver, 30);

            // 1.1设置页面超时等待时间,20S
            driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

            // 2.初始打开页面
            driver.get(page.getRequest().getUrl());

            // 3.获取kuai代理国内高匿代理ip数据表
            WebElement tableElement = WebDriverUtils.expWaitForElement(driver, By.xpath("//table"), 10);
            List<WebElement> trs= tableElement.findElements(By.xpath("//tbody//tr"));
            // 提取元素数据
            int listIndex=0;
            List<ProxyDTO> proxyDTOList =new ArrayList<>();
            for (int trindex=0;trindex<trs.size();++trindex) {
                WebElement tr =trs.get(trindex);
                log.info("tr [{}]",tr.getText());
                List<WebElement> tds =   tr.findElements(By.tagName("td"));
                log.info("ip [{}]",tds.get(0).getText());
                ProxyDTO proxyDO = ProxyDTO.builder()
                        .ip(tds.get(0).getText())
                        .port(tds.get(1).getText())
                        .ipType("HTTP")
                        .ipStatus("Y")
                        .lastCheckTime(DateUtil.parse(tds.get(4).getText()))
                        .location(tds.get(2).getText())
                        .responeSp("0")
                        .secretType("common")
                        .build();

                try{
                    if (proxyService.isValid(proxyDO)){
                        proxyDTOList.add(proxyDO);
                    }
                }catch (IOException ex){
                    log.error("[process] test proxy throw exception", ex);
                }

            }

            // 4.设置传递参数，pipeline进行持久化
            log.info("ipPoolDOList=>[{}] ", proxyDTOList);
            page.putField("ipPoolDTOList", proxyDTOList);

            try {
                sleep(5000);
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

    /**
     * 检测代理ip是否有效
     * @param proxyDO
     * @return
     */
    public static boolean isValid(ProxyDO proxyDO) {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyDO.getIp(), Integer.valueOf(proxyDO.getPort())));
        try {
            URLConnection httpCon = new URL("https://www.amazon.com/").openConnection(proxy);
            httpCon.setConnectTimeout(5000);
            httpCon.setReadTimeout(5000);
            int code = ((HttpURLConnection) httpCon).getResponseCode();
            log.debug("ip [{}] port[{}] code [{}]", proxyDO.getIp(), proxyDO.getPort(),code);
            return code == 200;
        } catch (IOException e) {
            return false;
        }
    }


    public static void main(String[] args) {
        System.out.println("0.step67=>抓取程序开启。");

//        Spider.create(new HawProductInfoProcessor())
//                .addUrl(SpiderUrl.SPIDER_HAW_INDEX)
//                .run();


    }

}

