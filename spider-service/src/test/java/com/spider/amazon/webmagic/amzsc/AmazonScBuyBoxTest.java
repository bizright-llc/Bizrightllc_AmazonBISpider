package com.spider.amazon.webmagic.amzsc;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.entity.AmzScBuyBox;
import com.spider.amazon.service.CommonSettingService;
import com.spider.amazon.webmagic.amzsc.AmazonScBuyBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

import java.time.LocalDate;

import static org.junit.Assert.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class AmazonScBuyBoxTest {

    @Autowired
    private SpiderConfig spiderConfig;

    @Autowired
    private CommonSettingService commonSettingService;

    @Test
    public void TestAmazonScButBoxDownloadFile() throws InterruptedException {

        AmazonScBuyBox process = new AmazonScBuyBox(spiderConfig, commonSettingService);
        process.setParseDate(LocalDate.of(2020,9,15));

        // 3.调用爬虫
        Spider spider= Spider.create(process);
        spider.thread(2);
        Request request = new Request(spiderConfig.getSpiderIndex());

        request.putExtra("craw_id","20200303165500");
        spider.addRequest(request);
        spider.start();
        Thread.sleep(300000);


    }

}