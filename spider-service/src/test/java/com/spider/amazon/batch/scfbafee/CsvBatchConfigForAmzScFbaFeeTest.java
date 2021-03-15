package com.spider.amazon.batch.scfbafee;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SpiderServiceApplication.class)
class CsvBatchConfigForAmzScFbaFeeTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JobLauncher csvJobLauncherForAmzScBuyBox;

    @Autowired
    private Job importJobForAmzScFbaFee;

    @Test
    public void testBatch1() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        csvJobLauncherForAmzScBuyBox.run(importJobForAmzScFbaFee, jobParameters);
        logger.info("testBatch1执行完成");
    }

}