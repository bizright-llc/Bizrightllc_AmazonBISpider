package com.spider.amazon.webmagic;

import cn.hutool.core.date.DateUtil;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.service.CommonSettingService;
import com.spider.amazon.webmagic.amzvc.AmazonVcDailyInventoryHealthManufacturing;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

import static org.mockito.Mockito.spy;

class AmazonVcDailyInventoryHealthManufacturingTest {

    @Spy
    private SpiderConfig spiderConfig;

    @Autowired
    private CommonSettingService commonSettingService;

    @BeforeMethod
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void TestAmazonScButBoxDownloadFile() throws InterruptedException {

        spiderConfig = spy(new SpiderConfig());

        Mockito.doReturn("/Users/shaochinlin/Documents/Bizright/BI/BiSpider/Download/").when(spiderConfig).getDownloadPath();
        Mockito.doReturn("/Users/shaochinlin/Documents/Bizright/BI/BiSpider/cookieVc.json").when(spiderConfig).getAmzVcCookieFilepath();
        Mockito.doReturn("https://www.google.com/").when(spiderConfig).getSpiderIndex();

        // 3.调用爬虫
        Spider spider= Spider.create(new AmazonVcDailyInventoryHealthManufacturing(spiderConfig, commonSettingService));
        spider.thread(2);
        Request request = new Request(spiderConfig.getSpiderIndex());

        String crawId = DateUtil.format(DateUtil.date(), DateFormat.YEAR_MONTH_DAY_yyyyMMddHHmmss1);
        request.putExtra("craw_id",crawId);
        spider.addRequest(request);
        spider.start();
        Thread.sleep(300000);

    }

}