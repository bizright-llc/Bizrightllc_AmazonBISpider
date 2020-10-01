package com.spider.amazon.webmagic.amzsc;

import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.webmagic.amzsc.AmazonScFbaInventory;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

import static org.mockito.Mockito.spy;

class AmazonScFbaInventoryTest {

    @Spy
    private SpiderConfig spiderConfig;

    @BeforeMethod
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void TestAmazonScButBoxDownloadFile() throws InterruptedException {

        spiderConfig = spy(new SpiderConfig());

        Mockito.doReturn("/Users/shaochinlin/Documents/Bizright/BI/BiSpider").when(spiderConfig).getDownloadPath();
        Mockito.doReturn("/Users/shaochinlin/Documents/Bizright/BI/BiSpider/cookieSc.json").when(spiderConfig).getAmzScCookieFilepath();
        Mockito.doReturn("https://www.google.com/").when(spiderConfig).getSpiderIndex();

        // 3.调用爬虫
        Spider spider= Spider.create(new AmazonScFbaInventory(spiderConfig));
        spider.thread(2);
        Request request = new Request(spiderConfig.getSpiderIndex());

        request.putExtra("craw_id","20200303165500");
        spider.addRequest(request);
        spider.start();
        Thread.sleep(300000);


    }

}