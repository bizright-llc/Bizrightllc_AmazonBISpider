package com.spider.amazon.batch.vcdailysales;

import com.spider.SpiderServiceApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class CsvBatchConfigForAmzDailySalesTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JobLauncher csvJobLauncherForAmzDailySales;

    @Autowired
    private Job importJobForAmzDailySales;

    @Test
    public void testBatch1() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        csvJobLauncherForAmzDailySales.run(importJobForAmzDailySales, jobParameters);
        logger.info("testBatch1执行完成");
    }

    @Test
    public void testFile() throws IOException {

        File csvFile = new File("/Users/shaochinlin/Documents/Bizright/BI/BiSpider/Download/Sales Diagnostic_Detail View_US (1).csv");

        Path source = Paths.get(csvFile.getPath());

        Files.move(source, source.resolveSibling("Sales Diagnostic_Detail View_US-FINISHED (1).csv"));

        File renameFile = new File("/Users/shaochinlin/Documents/Bizright/BI/BiSpider/Download/Sales Diagnostic_Detail View_US-FINISHED (1).csv");

        assertNotNull(renameFile);
    }
}