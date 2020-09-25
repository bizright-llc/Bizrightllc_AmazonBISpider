package com.spider.amazon.webmagic.amzvc;

import cn.hutool.core.date.DateUtil;
import com.spider.SpiderServiceApplication;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.service.CommonSettingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

import static org.mockito.Mockito.spy;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class AmazonVcDailyInventoryHealthTest {

    @Autowired
    private SpiderConfig spiderConfig;

    @Autowired
    private CommonSettingService commonSettingService;

//    @BeforeMethod
//    public void initMocks(){
//        MockitoAnnotations.initMocks(this);
//    }

    @Test
    public void TestDownloadInventoryHealthSourcingFile() throws InterruptedException {
        // 3.调用爬虫
        Spider spider= Spider.create(new AmazonVcDailyInventoryHealth(spiderConfig, commonSettingService));
        spider.thread(2);
        Request request = new Request(spiderConfig.getSpiderIndex());

        String crawId = DateUtil.format(DateUtil.date(), DateFormat.YEAR_MONTH_DAY_yyyyMMddHHmmss1);
        request.putExtra("craw_id",crawId);
        spider.addRequest(request);
        spider.start();
        Thread.sleep(300000);
    }

    @Test
    public void testDownloadInventoryHealthManufacturingFile() throws InterruptedException {
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