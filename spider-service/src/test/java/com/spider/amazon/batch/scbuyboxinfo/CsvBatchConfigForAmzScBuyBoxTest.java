package com.spider.amazon.batch.scbuyboxinfo;

import com.spider.SpiderServiceApplication;
import com.spider.amazon.service.impl.SpringBatchCallServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class CsvBatchConfigForAmzScBuyBoxTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JobLauncher csvJobLauncherForAmzScBuyBox;

    @Autowired
    private Job importJobForAmzScBuyBox;

    @Test
    public void testBatch1() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        csvJobLauncherForAmzScBuyBox.run(importJobForAmzScBuyBox, jobParameters);
        logger.info("testBatch1执行完成");
    }

}