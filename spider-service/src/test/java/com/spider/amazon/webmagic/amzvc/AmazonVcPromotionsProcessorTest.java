package com.spider.amazon.webmagic.amzvc;

import cn.hutool.core.date.DateUtil;
import com.spider.SpiderServiceApplication;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.cons.DateFormat;
import com.spider.amazon.mapper.VcPromotionInfoDOMapper;
import com.spider.amazon.mapper.VcPromotionProductInfoDOMapper;
import com.spider.amazon.model.VcPromotionInfoDO;
import com.spider.amazon.model.VcPromotionProcessorConfig;
import com.spider.amazon.model.VcPromotionProductInfoDO;
import com.spider.amazon.service.CommonSettingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.spy;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class AmazonVcPromotionsProcessorTest {

    @Autowired
    private SpiderConfig spiderConfig;

    @Autowired
    private VcPromotionInfoDOMapper vcPromotionInfoDOMapper;

    @Autowired
    private VcPromotionProductInfoDOMapper vcPromotionProductInfoDOMapper;

    @Autowired
    private CommonSettingService commonSettingService;

//    @Before
//    public void init(){
//        vcPromotionInfoDOMapper = SpringContextUtils.getBean(VcPromotionInfoDOMapper.class);
//    }

    @Test
    public void TestVcPromotionInsert(){
        VcPromotionInfoDO newRecord = new VcPromotionInfoDO();
        newRecord.setCrawId("test");
        newRecord.setStartDate(LocalDateTime.now());
        newRecord.setEndDate(LocalDateTime.now());
        newRecord.setCreatedOn(LocalDateTime.now());

        vcPromotionInfoDOMapper.insert(newRecord);
    }

    @Test
    public void TestVcPromotionInsertSelective(){
        VcPromotionInfoDO newRecord = new VcPromotionInfoDO();
        newRecord.setCrawId("test");
        newRecord.setStartDate(LocalDateTime.now());
        newRecord.setEndDate(LocalDateTime.now());
        newRecord.setCreatedOn(LocalDateTime.now());

        vcPromotionInfoDOMapper.insertSelective(newRecord);
    }

    @Test
    public void TestVcPromotionBatchInsert(){
        VcPromotionInfoDO newRecord = new VcPromotionInfoDO();
        newRecord.setCrawId("test");
        newRecord.setStartDate(LocalDateTime.now());
        newRecord.setEndDate(LocalDateTime.now());
        newRecord.setCreatedOn(LocalDateTime.now());

        VcPromotionInfoDO newRecord2 = new VcPromotionInfoDO();
        newRecord2.setCrawId("test");
        newRecord2.setStartDate(LocalDateTime.now());
        newRecord2.setEndDate(LocalDateTime.now());
        newRecord2.setCreatedOn(LocalDateTime.now());

        vcPromotionInfoDOMapper.insertBatch(Arrays.asList(newRecord, newRecord2));
    }

    @Test
    public void testVcPromotionProductInsert(){
        VcPromotionProductInfoDO productInfoDO = new VcPromotionProductInfoDO();
        productInfoDO.setCrawId("test");
        productInfoDO.setAmazonPriceStr("1234.1234");
        productInfoDO.setAmazonPrice(new BigDecimal("1234.1234").setScale(2, BigDecimal.ROUND_HALF_UP));

        vcPromotionProductInfoDOMapper.insert(productInfoDO);
    }

    @Test
    public void testVcPromotionProductBatchInsert(){
        VcPromotionProductInfoDO productInfoDO = new VcPromotionProductInfoDO();
        productInfoDO.setCrawId("test");
        productInfoDO.setAmazonPriceStr("1234.1234");
        productInfoDO.setAmazonPrice(new BigDecimal("1234.1234").setScale(2, BigDecimal.ROUND_HALF_UP));

        VcPromotionProductInfoDO productInfoDO2 = new VcPromotionProductInfoDO();
        productInfoDO2.setCrawId("test");
        productInfoDO2.setLikelyPriceStr("1234.1234");
        productInfoDO2.setLikelyPrice(new BigDecimal("1234.1234").setScale(2, BigDecimal.ROUND_HALF_UP));

        VcPromotionProductInfoDO productInfoDO3 = new VcPromotionProductInfoDO();
        productInfoDO3.setCrawId("test");
        productInfoDO3.setWebsitePriceStr("1234.1234");
        productInfoDO3.setWebsitePrice(new BigDecimal("1234.1234").setScale(2, BigDecimal.ROUND_HALF_UP));

        vcPromotionProductInfoDOMapper.insertBatch(Arrays.asList(productInfoDO, productInfoDO2, productInfoDO3));
    }



    @Test
    public void TestAmazonVcPromotionProcessor() throws InterruptedException {

        // 3.调用爬虫
        Spider spider= Spider.create(new AmazonVcPromotionsProcessor(spiderConfig, commonSettingService));
        spider.addPipeline(new AmazonVcPromotionsPipeline());
        spider.thread(2);
        Request request = new Request(spiderConfig.getSpiderIndex());

        String crawId = DateUtil.format(DateUtil.date(), DateFormat.YEAR_MONTH_DAY_yyyyMMddHHmmss1);

        request.putExtra("craw_id",crawId);
        spider.addRequest(request);
        spider.start();
        Thread.sleep(300000);

    }

    @Test
    public void TestAmazonVcCustomPromotionProcessor() throws InterruptedException {

        AmazonVcPromotionsCustomProcessor processor = new AmazonVcPromotionsCustomProcessor(spiderConfig, commonSettingService, vcPromotionInfoDOMapper);

        List<String> asinList = new ArrayList<>();
        asinList.add("B087YZMVJB");
        asinList.add("B087YT4F8V");
        asinList.add("B087XNG36S");
        asinList.add("B087XNBKSZ");
        asinList.add("B088Z2DFR7");
        asinList.add("B088Z5LVJ8");
        asinList.add("B08BBZ7W4B");
        asinList.add("B08BCFPTJ6");
        asinList.add("B08B1G7FTV");
        asinList.add("B089ZVPPQ2");
        asinList.add("B08B6HXHCH");
        asinList.add("B08B6J82YJ");

        VcPromotionProcessorConfig config = new VcPromotionProcessorConfig();

        config.setAsins(asinList);

        processor.setVcPromotionProcessorConfig(config);

        // 3.调用爬虫
        Spider spider= Spider.create(processor);
        spider.addPipeline(new AmazonVcPromotionsPipeline());
        spider.thread(2);
        Request request = new Request(spiderConfig.getSpiderIndex());

        String crawId = DateUtil.format(DateUtil.date(), DateFormat.YEAR_MONTH_DAY_yyyyMMddHHmmss1);

        request.putExtra("craw_id",crawId);
        spider.addRequest(request);
        spider.start();
        Thread.sleep(300000);

    }

    @Test
    public void testAmazonVcCustomPromotionProcessorDownloadAllPromotion() throws InterruptedException {

        AmazonVcPromotionsCustomProcessor processor = new AmazonVcPromotionsCustomProcessor(spiderConfig, commonSettingService, vcPromotionInfoDOMapper);

        List<String> asinList = new ArrayList<>();

        VcPromotionProcessorConfig config = new VcPromotionProcessorConfig();

        config.setAsins(asinList);
        config.setSkipExist(true);

        processor.setVcPromotionProcessorConfig(config);

        // 3.调用爬虫
        Spider spider= Spider.create(processor);
        spider.addPipeline(new AmazonVcPromotionsPipeline());
        spider.thread(2);
        Request request = new Request(spiderConfig.getSpiderIndex());

        String crawId = DateUtil.format(DateUtil.date(), DateFormat.YEAR_MONTH_DAY_yyyyMMddHHmmss1);

        request.putExtra("craw_id",crawId);
        spider.addRequest(request);
        spider.start();
        Thread.sleep(300000);

    }

}