package com.spider.amazon.webmagic;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.webmagic.amzvc.AmazonVcPromotionsPipeline;
import com.spider.amazon.webmagic.amzvc.AmazonVcPromotionsProcessor;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testng.annotations.BeforeMethod;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
//@SpringBootTest(classes= SpiderServiceApplication.class)
class AmazonScBuyBoxTest {

    @Spy
    private SpiderConfig spiderConfig;

    @BeforeMethod
    public void initMocks(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void TestAmazonScButBoxDownloadFile() throws InterruptedException {

        spiderConfig = spy(new SpiderConfig());

        Mockito.doReturn("/Users/shaochinlin/Documents/Bizright/BI/BiSpider").when(spiderConfig).getDownloadPathLinux();
        Mockito.doReturn("/Users/shaochinlin/Documents/Bizright/BI/BiSpider/cookieSc.json").when(spiderConfig).getAmzScCookieFilepath();
        Mockito.doReturn("https://www.google.com/").when(spiderConfig).getSpiderIndex();

        // 3.调用爬虫
        Spider spider= Spider.create(new AmazonScBuyBox(spiderConfig));
        spider.thread(2);
        Request request = new Request(spiderConfig.getSpiderIndex());

        request.putExtra("craw_id","20200303165500");
        spider.addRequest(request);
        spider.start();
        Thread.sleep(300000);


    }

}