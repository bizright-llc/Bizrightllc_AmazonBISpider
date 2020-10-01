package com.spider.amazon.webmagic.amzsc;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.service.CommonSettingService;
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

import static org.mockito.Mockito.spy;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class AmazonScFbaInventoryTest {

    @Autowired
    private SpiderConfig spiderConfig;

    @Autowired
    private CommonSettingService commonSettingService;

    @Test
    public void testAmazonScFbaInventoryDownloadFile() throws InterruptedException {

        // 3.调用爬虫
        Spider spider= Spider.create(new AmazonScFbaInventory(spiderConfig, commonSettingService));
        spider.thread(2);
        Request request = new Request(spiderConfig.getSpiderIndex());

        request.putExtra("craw_id","20200303165500");
        spider.addRequest(request);
        spider.start();
        Thread.sleep(300000);

    }

}