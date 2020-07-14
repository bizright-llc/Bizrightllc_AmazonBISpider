package com.spider.amazon.webmagic.amzvc;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.webmagic.AmazonScBuyBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testng.annotations.BeforeMethod;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class AmazonVcPromotionsProcessorTest {

    @Autowired
    private SpiderConfig spiderConfig;

//    @BeforeMethod
//    public void initMocks(){
//        MockitoAnnotations.initMocks(this);
//    }

    @Test
    public void TestAmazonScButBoxDownloadFile() throws InterruptedException {

//        spiderConfig = spy(new SpiderConfig());
//
//        Mockito.doReturn("/Users/shaochinlin/Documents/Bizright/BI/BiSpider").when(spiderConfig).getDownloadPathLinux();
//        Mockito.doReturn("/Users/shaochinlin/Documents/Bizright/BI/BiSpider/cookieVc.json").when(spiderConfig).getAmzScCookieFilepath();
//        Mockito.doReturn("https://www.google.com/").when(spiderConfig).getSpiderIndex();

        // 3.调用爬虫
        Spider spider= Spider.create(new AmazonVcPromotionsProcessor(spiderConfig));
        spider.addPipeline(new AmazonVcPromotionsPipeline());
        spider.thread(2);
        Request request = new Request(spiderConfig.getSpiderIndex());

        request.putExtra("craw_id","20200303165500");
        spider.addRequest(request);
        spider.start();
        Thread.sleep(300000);

    }

}