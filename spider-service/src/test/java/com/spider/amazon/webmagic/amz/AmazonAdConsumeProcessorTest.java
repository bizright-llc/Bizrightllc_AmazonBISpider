package com.spider.amazon.webmagic.amz;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.service.AmazonAdService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class AmazonAdConsumeProcessorTest {

    @Autowired
    private SpiderConfig spiderConfig;

    @Autowired
    private AmazonAdService amazonAdService;

    @Test
    public void testAdConsume() throws InterruptedException {

        // 3.调用爬虫
        Spider spider= Spider.create(new AmazonAdConsumeProcessor(spiderConfig, amazonAdService));
        spider.thread(2);
        Request request = new Request(spiderConfig.getSpiderIndex());

        request.putExtra("craw_id","20200303165500");
        spider.addRequest(request);
        spider.start();

        while (spider.getStatus() != Spider.Status.Stopped){
            Thread.sleep(10000);
        }

    }

}