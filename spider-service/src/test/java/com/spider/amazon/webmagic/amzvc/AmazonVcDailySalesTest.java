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

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.mockito.Mockito.spy;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class AmazonVcDailySalesTest {

    @Autowired
    private SpiderConfig spiderConfig;

    @Autowired
    private CommonSettingService commonSettingService;

//    @BeforeMethod
//    public void initMocks(){
//        MockitoAnnotations.initMocks(this);
//    }

//    @Test
//    public void TestDownloadDailySalesSourcingFile() throws InterruptedException {
//
//        // 3.调用爬虫
//        Spider spider= Spider.create(new AmazonVcSourcingDailySales(spiderConfig, commonSettingService));
//        spider.thread(2);
//        Request request = new Request(spiderConfig.getSpiderIndex());
//
//        String crawId = DateUtil.format(DateUtil.date(), DateFormat.YEAR_MONTH_DAY_yyyyMMddHHmmss1);
//        request.putExtra("craw_id",crawId);
//        spider.addRequest(request);
//        spider.start();
//        Thread.sleep(300000);
//
//    }

    @Test
    public void TestDownloadDailySalesManufaturingFile() throws InterruptedException {

        // 3.调用爬虫
        Spider spider= Spider.create(new AmazonVcDailySales(spiderConfig, commonSettingService));
        spider.thread(2);
        Request request = new Request(spiderConfig.getSpiderIndex());

        String crawId = DateUtil.format(DateUtil.date(), DateFormat.YEAR_MONTH_DAY_yyyyMMddHHmmss1);
        request.putExtra("craw_id",crawId);
        spider.addRequest(request);
        spider.start();
        Thread.sleep(300000);

    }

    @Test
    public void testDownloadDailySalesFileWithDate() throws InterruptedException {

        // 3.调用爬虫
        LocalDate startDate = LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY);
        System.out.println(String.format("first date: %s", startDate));
        LocalDate lastDate = LocalDate.now().minusWeeks(1).with(DayOfWeek.SUNDAY);
        System.out.println(String.format("first date: %s", lastDate));

        Spider spider = null;

        while (startDate.compareTo(lastDate) <= 0){

            if(spider == null || spider.getStatus() == Spider.Status.Stopped){

                try{
                    AmazonVcDailySales process = new AmazonVcDailySales(spiderConfig, commonSettingService);
                    process.setParseDate(startDate);

                    spider = Spider.create(process);

                    spider.thread(2);
                    Request request = new Request(spiderConfig.getSpiderIndex());

                    spider.addRequest(request);
                    spider.start();

                    startDate = startDate.plusDays(1);
                }catch (Exception ex){
                    System.out.println(String.format("get daily sales %s failed", startDate));

                    ex.printStackTrace();
                }

            }else{
                Thread.sleep(5000);
            }
        }

        Thread.sleep(300000);

    }
}