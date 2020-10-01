package com.spider.amazon.service.impl;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.config.SpiderConfig;
import com.spider.amazon.service.FbaInventoryReportDealService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class FbaInventoryReportDealServiceImplTest {

    @Autowired
    private FbaInventoryReportDealService fbaInventoryReportDealService;

    @Autowired
    private SpiderConfig spiderConfig;

    @Test
    void testDealFbaInventoryReport() {

        String filename = "FbaInventory-20201001.csv";

        fbaInventoryReportDealService.dealFbaInventoryReport(filename, spiderConfig.getScFBAInventoryDownloadPath());

    }
}